let currentRole = "Manager";
const MINIMUM_STAFF_POLICY_ID = 1;

const defaultTaskColors = {
    "Desk": "#4da3ff",
    "Check-in": "#f4c542",
    "Picking": "#4ecb71",
    "Shelving": "#d96df0",
    "Meeting": "#ff9f68",
    "Lunch": "#ffcf5a",
    "Event": "#45c7c7",
    "Event Prep": "#8f7cff",
    "Closing-15mins": "#ff7b7b",
    "Training": "#5f8bff",
    "Block": "#dad8c9",
    "Bell": "#ff8aa1",
    "Roaming": "#6ed3ff",
    "Lunch/Check-in": "#f7b267",
    "Lunch/Bell": "#ffb3c7",
    "Lunch/Roaming": "#8ee3ef",
    "Optional": "#c7d2e2",
    "Off-site": "#444444"
};

const taskNames = [
    "Desk",
    "Check-in",
    "Picking",
    "Shelving",
    "Meeting",
    "Lunch",
    "Event",
    "Event Prep",
    "Closing-15mins",
    "Training",
    "Block",
    "Bell",
    "Roaming",
    "Lunch/Check-in",
    "Lunch/Bell",
    "Lunch/Roaming",
    "Optional",
    "Off-site"
];

const timeSlots = [
    "9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
    "1:00 PM", "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM"
];

const defaultStaff = [
    {id: 1, name: "Emma Li"},
    {id: 2, name: "Noah Patel"},
    {id: 3, name: "Olivia Chen"},
    {id: 4, name: "Lucas Singh"},
    {id: 5, name: "Ava Wilson"},
    {id: 6, name: "Ethan Brown"},
    {id: 7, name: "Sophia Kumar"}
];

const defaultShiftDate = "2026-06-10";
const API_BASE_PATH = "/1.0";
const AUTO_RULE_MODAL_ID = "autoScheduleRuleModal";
const AUTO_RULE_LIST_ID = "autoRuleList";
const AUTO_RULE_SELECT_ALL_ID = "autoRuleSelectAllCheckbox";
const AUTO_RULE_CONFIRM_ID = "autoRuleConfirmCheckbox";
const AUTO_RULE_CONFIRM_TEXT_ID = "autoRuleConfirmText";
const AUTO_RULE_CONFIRM_TOGGLE_ID = "autoRuleConfirmToggle";
const AUTO_RULE_ITEM_CLASS = "auto-rule-item-checkbox";

let cachedAutoScheduleRules = [];
let isAutoScheduleInProgress = false;

function buildApiUrl(url) {
    if (/^https?:\/\//i.test(url)) {
        return url;
    }

    if (url.startsWith(`${API_BASE_PATH}/`)) {
        return url;
    }

    if (url.startsWith("/")) {
        return `${API_BASE_PATH}${url}`;
    }

    return `${API_BASE_PATH}/${url}`;
}

function getAuthToken() {
    return localStorage.getItem("shiftPlannerAuthToken") || "";
}

function buildApiHeaders(requiresAuth = true, extraHeaders = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...extraHeaders
    };

    if (requiresAuth) {
        const token = getAuthToken();
        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }
    }

    return headers;
}

async function apiFetch(url, options = {}, requiresAuth = true) {
    const {headers = {}, ...rest} = options;
    const response = await fetch(buildApiUrl(url), {
        ...rest,
        headers: buildApiHeaders(requiresAuth, headers)
    });

    if (response.status === 401 && requiresAuth) {
        localStorage.removeItem("shiftPlannerAuthToken");
        localStorage.removeItem("shiftPlannerCurrentUsername");
        localStorage.removeItem("shiftPlannerCurrentRole");
        window.location.href = "index.html";
    }

    return response;
}

document.addEventListener("DOMContentLoaded", () => {
    const managerBtn = document.getElementById("managerRoleBtn");
    const librarianBtn = document.getElementById("librarianRoleBtn");

    if (managerBtn && librarianBtn) {
        managerBtn.addEventListener("click", () => setRole("Manager"));
        librarianBtn.addEventListener("click", () => setRole("Librarian"));
    }

    initializeStorage();
});

function initializeStorage() {
    if (!localStorage.getItem("shiftPlannerStaff")) {
        localStorage.setItem("shiftPlannerStaff", JSON.stringify(defaultStaff));
    }

    if (!localStorage.getItem("shiftPlannerMinimumStaff")) {
        localStorage.setItem("shiftPlannerMinimumStaff", "3");
    }

    if (!localStorage.getItem("shiftPlannerRemovedStaffIds")) {
        localStorage.setItem("shiftPlannerRemovedStaffIds", JSON.stringify([]));
    }

    if (!localStorage.getItem("shiftPlannerSchedule")) {
        localStorage.setItem("shiftPlannerSchedule", JSON.stringify(createDefaultSchedule()));
    }

    if (!localStorage.getItem("shiftPlannerShiftDate")) {
        localStorage.setItem("shiftPlannerShiftDate", defaultShiftDate);
    }

    if (!localStorage.getItem("shiftPlannerDailyAssignments")) {
        localStorage.setItem("shiftPlannerDailyAssignments", JSON.stringify({
            roster: "",
            banking: "",
            bankingBackup: "",
            inspection: ""
        }));
    }

    if (!localStorage.getItem("shiftPlannerCurrentRole")) {
        localStorage.setItem("shiftPlannerCurrentRole", "Manager");
    }

    if (!localStorage.getItem("shiftPlannerDynamicStaffIds")) {
        localStorage.setItem("shiftPlannerDynamicStaffIds", JSON.stringify([]));
    }
}

function setRole(role) {
    currentRole = role;
    document.getElementById("managerRoleBtn")?.classList.toggle("active", role === "Manager");
    document.getElementById("librarianRoleBtn")?.classList.toggle("active", role === "Librarian");

    const roleHint = document.getElementById("roleHint");
    const passwordHint = document.getElementById("passwordHint");

    if (roleHint) {
        roleHint.textContent =
            role === "Manager"
                ? "Example Manager Username: Manager"
                : "Example Librarian Username: Senior Librarian";
    }

    if (passwordHint) {
        passwordHint.textContent =
            role === "Manager"
                ? "Manager password: manager2026"
                : "Librarian password: librarian2026";
    }
}

function handleLogin() {
    loginWithApi();
}

async function loginWithApi() {
    const username = document.getElementById("username")?.value.trim();
    const password = document.getElementById("password")?.value.trim();
    const error = document.getElementById("loginError");

    if (error) {
        error.style.display = "none";
    }

    if (!username || !password) {
        if (error) {
            error.textContent = "Please enter both username and password.";
            error.style.display = "block";
        }
        return;
    }

    const loginPayload = {
        role: currentRole === "Manager" ? "manager" : "staff",
        username,
        password
    };

    try {
        const response = await apiFetch("api/auth/login", {
            method: "POST",
            body: JSON.stringify(loginPayload)
        }, false);

        if (!response.ok) {
            if (error) {
                error.textContent = "Incorrect credentials. Please check your username, password, and selected role.";
                error.style.display = "block";
            }
            return;
        }

        const loginResponse = await response.json();
        const apiRole = (loginResponse.role || "").toLowerCase();
        const appRole = apiRole === "manager" ? "Manager" : "Librarian";

        if (loginResponse.token) {
            localStorage.setItem("shiftPlannerAuthToken", loginResponse.token);
        }
        localStorage.setItem("shiftPlannerCurrentRole", appRole);
        localStorage.setItem("shiftPlannerCurrentUsername", loginResponse.username || username);

        window.location.href = appRole === "Manager" ? "manager.html" : "librarian.html";
    } catch (e) {
        if (error) {
            error.textContent = "Incorrect credentials. Please check your username, password, and selected role.";
            error.style.display = "block";
        }
    }
}

function getCurrentRole() {
    return localStorage.getItem("shiftPlannerCurrentRole") || "Manager";
}

function logout() {
    localStorage.removeItem("shiftPlannerAuthToken");
    localStorage.removeItem("shiftPlannerCurrentUsername");
    window.location.href = "index.html";
}

function showSection(sectionId) {
    ["staffSection", "policySection", "taskSection", "reviewSection"].forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.classList.toggle("hidden", id !== sectionId);
        }
    });
}

/* ---------------- DATE ---------------- */

function getStoredShiftDate() {
    return localStorage.getItem("shiftPlannerShiftDate") || defaultShiftDate;
}

function getTodayDateString() {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, "0");
    const dd = String(today.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`;
}

function formatShiftDate(dateString) {
    if (!dateString) return "No date selected";

    const [year, month, day] = dateString.split("-").map(Number);
    const date = new Date(year, month - 1, day);

    return date.toLocaleDateString("en-NZ", {
        weekday: "long",
        day: "numeric",
        month: "long",
        year: "numeric"
    });
}

function updateScheduleTitles() {
    const formattedDate = formatShiftDate(getStoredShiftDate());

    const shiftTitle = document.getElementById("shiftScheduleTitle");
    const reviewTitle = document.getElementById("reviewScheduleTitle");

    if (shiftTitle) {
        shiftTitle.textContent = `Auckland Library — ${formattedDate}`;
    }

    if (reviewTitle) {
        reviewTitle.textContent = `Auckland Library — ${formattedDate}`;
    }
}

function toUiTimeSlot(apiTimeSlot) {
    const match = /^(\d{2}):(\d{2})/.exec(apiTimeSlot || "");
    if (!match) {
        return null;
    }

    let hours = Number(match[1]);
    const minutes = match[2];
    const meridiem = hours >= 12 ? "PM" : "AM";
    if (hours === 0) {
        hours = 12;
    } else if (hours > 12) {
        hours -= 12;
    }

    return `${hours}:${minutes} ${meridiem}`;
}

function mapApiScheduleToLocalState(apiSchedule) {
    const assignments = Array.isArray(apiSchedule?.assignments) ? apiSchedule.assignments : [];
    const staffById = new Map(getStaff().map(staff => [Number(staff.id), staff]));

    const inferredStaffIds = [];
    assignments.forEach(assignment => {
        const staffId = Number(assignment?.staffId);
        if (Number.isFinite(staffId) && !inferredStaffIds.includes(staffId)) {
            inferredStaffIds.push(staffId);
        }
    });

    const activeIds = inferredStaffIds.length > 0
        ? inferredStaffIds
        : (getDynamicStaffIds().length > 0 ? getDynamicStaffIds() : getStaff().slice(0, 1).map(staff => Number(staff.id)));

    const rowCount = timeSlots.length;
    const colCount = Math.max(activeIds.length, 1);
    const schedule = Array.from({length: rowCount}, () => Array.from({length: colCount}, () => "Optional"));
    const taskIdToName = Object.fromEntries(Object.entries(taskNameToApiTaskId).map(([taskName, taskId]) => [String(taskId), taskName]));

    const staffIndexById = new Map(activeIds.map((staffId, index) => [Number(staffId), index]));
    const timeIndexByUiSlot = new Map(timeSlots.map((slot, index) => [slot, index]));

    assignments.forEach(assignment => {
        const staffIndex = staffIndexById.get(Number(assignment?.staffId));
        const uiSlot = toUiTimeSlot(assignment?.timeSlot || "");
        const rowIndex = uiSlot ? timeIndexByUiSlot.get(uiSlot) : undefined;
        const taskName = taskIdToName[String(assignment?.taskId)] || "Optional";

        if (typeof rowIndex === "number" && typeof staffIndex === "number") {
            schedule[rowIndex][staffIndex] = taskName;
        }
    });

    const toStaffName = staffId => staffById.get(Number(staffId))?.name || "";

    localStorage.setItem("shiftPlannerDynamicStaffIds", JSON.stringify(activeIds));
    localStorage.setItem("shiftPlannerSchedule", JSON.stringify(schedule));
    localStorage.setItem("shiftPlannerDailyAssignments", JSON.stringify({
        roster: toStaffName(apiSchedule?.rosterStaffId),
        banking: toStaffName(apiSchedule?.bankingStaffId),
        bankingBackup: toStaffName(apiSchedule?.backupStaffId),
        inspection: toStaffName(apiSchedule?.inspectionStaffId)
    }));
}

async function fetchScheduleByDate(date) {
    const response = await apiFetch(`/api/schedules/${encodeURIComponent(date)}`);
    if (!response.ok) {
        if (response.status === 404) {
            return null;
        }
        throw new Error(`Failed to load schedule: ${response.status}`);
    }

    return response.json();
}

function resetToStarterSchedule() {
    localStorage.removeItem("shiftPlannerDynamicStaffIds");
    localStorage.removeItem("shiftPlannerSchedule");
    localStorage.removeItem("shiftPlannerDailyAssignments");

    const firstStaffId = Number(getStaff()?.[0]?.id);
    if (Number.isFinite(firstStaffId)) {
        saveDynamicStaffIds([firstStaffId]);
    }
    saveSchedule(createDefaultSchedule());
}

async function loadShiftDatePicker() {
    const shiftDateInput = document.getElementById("shiftDate");

    const selectedDate = shiftDateInput?.value || getStoredShiftDate();
    localStorage.setItem("shiftPlannerShiftDate", selectedDate || defaultShiftDate);

    if (selectedDate) {
        try {
            const apiSchedule = await fetchScheduleByDate(selectedDate);
            if (apiSchedule) {
                mapApiScheduleToLocalState(apiSchedule);
            } else {
                resetToStarterSchedule();
            }
        } catch (error) {
            console.error("Unable to load schedule from API.", error);
            resetToStarterSchedule();
        }
    }

    updateScheduleTitles();
    buildVisibleCalendars();
    loadNotes("shiftNotes");
    populateDutyDropdowns();
}

function loadReviewDatePicker() {
    const reviewDateInput = document.getElementById("reviewDate");
    if (reviewDateInput) {
        reviewDateInput.value = getStoredShiftDate();
    }
    updateScheduleTitles();
}

function setupReviewDatePickerVisibility() {
    const datePickerRow = document.getElementById("reviewDatePickerRow");
    if (!datePickerRow) return;

    const role = (localStorage.getItem("shiftPlannerCurrentRole") || "").trim().toLowerCase();
    const isSeniorLibrarian = role === "senior librarian" || role === "librarian" || role === "staff";

    datePickerRow.style.display = isSeniorLibrarian ? "none" : "";
}

function isManagerRole() {
    return (localStorage.getItem("shiftPlannerCurrentRole") || "").trim().toLowerCase() === "manager";
}

async function loadShiftDate() {
    const shiftDateInput = document.getElementById("shiftDate");
    if (!shiftDateInput) return;

    const selectedDate = shiftDateInput.value || defaultShiftDate;
    localStorage.setItem("shiftPlannerShiftDate", selectedDate);

    try {
        const apiSchedule = await fetchScheduleByDate(selectedDate);
        if (apiSchedule) {
            mapApiScheduleToLocalState(apiSchedule);
        } else {
            resetToStarterSchedule();
        }
    } catch (error) {
        console.error("Unable to load schedule from API.", error);
    }

    updateScheduleTitles();
    buildVisibleCalendars();
    loadNotes("shiftNotes");
    populateDutyDropdowns();

    shiftDateInput.value="";
}

async function loadReviewScheduleByPickedDate(showAlertOnError = true) {
    const reviewDateInput = document.getElementById("reviewDate");
    if (!reviewDateInput || !reviewDateInput.value) {
        return;
    }

    const pickedDate = reviewDateInput.value;
    localStorage.setItem("shiftPlannerShiftDate", pickedDate);

    try {
        const apiSchedule = await fetchScheduleByDate(pickedDate);
        if (apiSchedule) {
            mapApiScheduleToLocalState(apiSchedule);
        } else {
            resetToStarterSchedule();
        }
    } catch (error) {
        console.error("Unable to load review schedule from API.", error);
        resetToStarterSchedule();
        if (showAlertOnError) {
            alert("Unable to load schedule for the selected date.");
        }
    }

    updateScheduleTitles();
    buildVisibleCalendars();
    loadNotes("reviewNotes");
    loadDailyAssignmentsForReview();
}

/* ---------------- STAFF ---------------- */

function getStaff() {
    return JSON.parse(localStorage.getItem("shiftPlannerStaff")) || [];
}

function mapApiStaffToUi(staff) {
    const id = Number(staff?.staffId ?? staff?.id);
    if (!Number.isFinite(id)) {
        return null;
    }

    return {
        id,
        name: staff?.name || ""
    };
}

function normalizeApiStaffList(staffList) {
    if (!Array.isArray(staffList)) {
        return [];
    }

    return staffList
        .map(mapApiStaffToUi)
        .filter(Boolean);
}

async function fetchAndCacheStaff() {
    try {
        const response = await apiFetch("api/staff");
        if (!response.ok) {
            console.error(`Unable to load staff from API. Status: ${response.status}`);
            return getStaff();
        }

        const staffList = normalizeApiStaffList(await response.json());
        saveStaffList(staffList);

        const dynamicIds = getDynamicStaffIds().filter(id =>
            staffList.some(staff => staff.id === id)
        );
        saveDynamicStaffIds(dynamicIds);

        return staffList;
    } catch (error) {
        console.error("Unable to load staff from API.", error);
        return getStaff();
    }
}

function saveStaffList(staffList) {
    localStorage.setItem("shiftPlannerStaff", JSON.stringify(staffList));
}

function saveRemovedStaffIds(ids) {
    localStorage.setItem("shiftPlannerRemovedStaffIds", JSON.stringify(ids));
}

function getDynamicStaffIds() {
    return JSON.parse(localStorage.getItem("shiftPlannerDynamicStaffIds")) || [];
}

function saveDynamicStaffIds(ids) {
    localStorage.setItem("shiftPlannerDynamicStaffIds", JSON.stringify(ids));
}

function getActiveShiftStaff() {
    const allStaff = getStaff();
    const dynamicIds = getDynamicStaffIds();
    return dynamicIds
        .map(id => allStaff.find(staff => staff.id === id))
        .filter(Boolean);
}

function getFilteredStaff() {
    const staffList = getStaff();
    const searchInput = document.getElementById("staffSearch");
    const query = searchInput ? searchInput.value.trim().toLowerCase() : "";

    let filteredStaff = staffList;

    if (query) {
        filteredStaff = staffList.filter(staff =>
            staff.name.toLowerCase().includes(query)
        );
    }

    return filteredStaff.sort((a, b) => {
        const getSurname = name => {
            const parts = name.trim().split(/\s+/);
            return parts[parts.length - 1].toLowerCase();
        };

        const surnameA = getSurname(a.name);
        const surnameB = getSurname(b.name);

        if (surnameA !== surnameB) {
            return surnameA.localeCompare(surnameB);
        }

        return a.name.toLowerCase().localeCompare(b.name.toLowerCase());
    });
}

function renderStaffTable() {
    const tbody = document.querySelector("#staffTable tbody");
    if (!tbody) return;

    const staffList = getFilteredStaff();
    tbody.innerHTML = "";

    if (staffList.length === 0) {
        const row = document.createElement("tr");
        row.innerHTML = `<td colspan="2">No staff found.</td>`;
        tbody.appendChild(row);
        return;
    }

    staffList.forEach(staff => {
        const row = document.createElement("tr");
        row.innerHTML = `
      <td>${staff.name}</td>
      <td>
        <div class="actions-inline">
          <button class="btn" onclick="editStaff(${staff.id})">Edit</button>
          <button class="btn btn-danger" onclick="deleteStaff(${staff.id})">Delete</button>
        </div>
      </td>
    `;
        tbody.appendChild(row);
    });
}

function openStaffForm() {
    const formCard = document.getElementById("staffFormCard");
    if (formCard) {
        formCard.scrollIntoView({behavior: "smooth"});
    }
}

function resetStaffForm() {
    const title = document.getElementById("staffFormTitle");
    const name = document.getElementById("staffName");
    const editId = document.getElementById("editingStaffId");

    if (title) title.textContent = "Add Staff Member";
    if (name) name.value = "";
    if (editId) editId.value = "";
}

async function saveStaff() {
    const id = document.getElementById("editingStaffId")?.value;
    const name = document.getElementById("staffName")?.value.trim();

    if (!name) {
        alert("Please enter a staff name.");
        return;
    }

    const payload = {
        name
    };

    try {
        const response = id
            ? await apiFetch(`api/staff/${id}`, {
                method: "PUT",
                body: JSON.stringify(payload)
            })
            : await apiFetch("api/staff", {
                method: "POST",
                body: JSON.stringify(payload)
            });

        if (!response.ok) {
            console.error(`Unable to save staff. Status: ${response.status}`);
            alert("Failed to save staff record. Please try again.");
            return;
        }

        const staffList = normalizeApiStaffList(await response.json());
        saveStaffList(staffList);

        const currentDynamicIds = getDynamicStaffIds().filter(dynamicId =>
            staffList.some(staff => staff.id === dynamicId)
        );
        saveDynamicStaffIds(currentDynamicIds);

        renderStaffTable();
        resetStaffForm();
        refreshAllHeaders();
        populateDutyDropdowns();
        alert("Staff record saved.");
    } catch (error) {
        console.error("Unable to save staff.", error);
        alert("Failed to save staff record. Please try again.");
    }
}

function editStaff(id) {
    const staff = getStaff().find(item => item.id === id);
    if (!staff) return;

    document.getElementById("staffFormTitle").textContent = "Edit Staff Member";
    document.getElementById("staffName").value = staff.name;
    document.getElementById("editingStaffId").value = staff.id;

    openStaffForm();
}

async function deleteStaff(id) {
    const confirmed = confirm("Are you sure you want to delete this staff member?");
    if (!confirmed) return;

    try {
        const response = await apiFetch(`api/staff/${id}`, {
            method: "DELETE"
        });

        if (!response.ok) {
            console.error(`Unable to delete staff. Status: ${response.status}`);
            alert("Failed to delete staff member. Please try again.");
            return;
        }

        const staffList = normalizeApiStaffList(await response.json());
        saveStaffList(staffList);

        const dynamicIds = getDynamicStaffIds().filter(dynamicId =>
            staffList.some(staff => staff.id === dynamicId)
        );
        saveDynamicStaffIds(dynamicIds);

        renderStaffTable();
        refreshAllHeaders();
        populateDutyDropdowns();
    } catch (error) {
        console.error("Unable to delete staff.", error);
        alert("Failed to delete staff member. Please try again.");
    }
}

function addStaffColumn() {
    const allStaff = getStaff();
    const dynamicIds = getDynamicStaffIds();

    if (dynamicIds.length >= allStaff.length) {
        alert("All staff have already been added.");
        return;
    }

    const firstAvailable = allStaff.find(staff => !dynamicIds.includes(staff.id));
    if (!firstAvailable) return;

    dynamicIds.push(firstAvailable.id);
    saveDynamicStaffIds(dynamicIds);
    normalizeScheduleToStaffCount();
    buildVisibleCalendars();
    populateDutyDropdowns();
}

function updateStaffColumn(index, staffId) {
    const dynamicIds = getDynamicStaffIds();
    dynamicIds[index] = Number(staffId);
    saveDynamicStaffIds(dynamicIds);
    buildVisibleCalendars();
    populateDutyDropdowns();
}

/* ---------------- POLICIES ---------------- */

function normalizePolicy(policy) {
    const policyId = Number(policy?.policyId ?? policy?.policy_id);
    const rawParam1 = policy?.param1 ?? policy?.param_1;
    const param1 = Number(rawParam1);

    return {
        policyId: Number.isFinite(policyId) ? policyId : null,
        description: policy?.description || "",
        param1: Number.isFinite(param1) ? param1 : 0
    };
}

function getPolicyById(policies, policyId) {
    if (!Array.isArray(policies) || policies.length === 0) {
        return null;
    }

    return policies.find(policy => policy.policyId === policyId) || null;
}

function getDefaultAutoScheduleRules() {
    return [{
        policyId: MINIMUM_STAFF_POLICY_ID,
        description: "Minimum staffing requirement",
        param1: Number(localStorage.getItem("shiftPlannerMinimumStaff") || "3")
    }];
}

async function fetchAutoScheduleRules() {
    try {
        const response = await apiFetch("api/policies");
        if (!response.ok) {
            return getDefaultAutoScheduleRules();
        }

        const policies = (await response.json())
            .map(normalizePolicy)
            .filter(policy => Number.isFinite(policy.policyId))
            .sort((a, b) => a.policyId - b.policyId);

        return policies.length > 0 ? policies : getDefaultAutoScheduleRules();
    } catch (error) {
        console.error("Unable to load auto-schedule rules.", error);
        return getDefaultAutoScheduleRules();
    }
}

function getAutoRuleModalElements() {
    return {
        modal: document.getElementById(AUTO_RULE_MODAL_ID),
        list: document.getElementById(AUTO_RULE_LIST_ID),
        selectAll: document.getElementById(AUTO_RULE_SELECT_ALL_ID),
        confirm: document.getElementById(AUTO_RULE_CONFIRM_ID),
        confirmText: document.getElementById(AUTO_RULE_CONFIRM_TEXT_ID),
        confirmToggle: document.getElementById(AUTO_RULE_CONFIRM_TOGGLE_ID)
    };
}

function setAutoRuleConfirmLoadingState(isLoading) {
    const {confirm, selectAll, confirmText, confirmToggle} = getAutoRuleModalElements();

    if (confirm) {
        confirm.disabled = isLoading;
    }
    if (selectAll) {
        selectAll.disabled = isLoading;
    }

    getAutoRuleItemCheckboxes().forEach(checkbox => {
        checkbox.disabled = isLoading;
    });

    if (confirmText) {
        confirmText.textContent = isLoading ? "Confirming..." : "Confirm";
    }

    if (confirmToggle) {
        confirmToggle.classList.toggle("is-loading", isLoading);
    }
}

function buildAutoRuleLabelText(policy) {
    const fallbackLabel = `Rule ${policy.policyId}`;
    const description = (policy.description.replace("{1}", policy.param1) || "").trim();
    return description ? `${fallbackLabel}: ${description}` : fallbackLabel;
}

function renderAutoScheduleRuleList(rules) {
    const {list} = getAutoRuleModalElements();
    if (!list) return;

    list.innerHTML = "";

    rules.forEach(policy => {
        const row = document.createElement("label");
        row.className = "auto-rule-item";

        const checkFrame = document.createElement("span");
        checkFrame.className = "auto-rule-item-checkframe";

        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.checked = true;
        checkbox.className = AUTO_RULE_ITEM_CLASS;
        checkbox.value = String(policy.policyId);
        checkbox.addEventListener("change", updateAutoRuleSelectAllState);
        checkFrame.appendChild(checkbox);

        const textFrame = document.createElement("span");
        textFrame.className = "auto-rule-item-content";
        textFrame.textContent = buildAutoRuleLabelText(policy);

        row.appendChild(checkFrame);
        row.appendChild(textFrame);
        list.appendChild(row);
    });

    updateAutoRuleSelectAllState();
}

function getAutoRuleItemCheckboxes() {
    return Array.from(document.querySelectorAll(`.${AUTO_RULE_ITEM_CLASS}`));
}

function getSelectedAutoRuleIds() {
    return getAutoRuleItemCheckboxes()
        .filter(checkbox => checkbox.checked)
        .map(checkbox => Number(checkbox.value))
        .filter(Number.isFinite);
}

function updateAutoRuleSelectAllState() {
    const {selectAll} = getAutoRuleModalElements();
    if (!selectAll) return;

    const checkboxes = getAutoRuleItemCheckboxes();
    const checkedCount = checkboxes.filter(checkbox => checkbox.checked).length;
    selectAll.checked = checkboxes.length > 0 && checkedCount === checkboxes.length;
    selectAll.indeterminate = checkedCount > 0 && checkedCount < checkboxes.length;
}

function handleAutoRuleSelectAllChange() {
    const {selectAll} = getAutoRuleModalElements();
    if (!selectAll) return;

    getAutoRuleItemCheckboxes().forEach(checkbox => {
        checkbox.checked = selectAll.checked;
    });

    updateAutoRuleSelectAllState();
}

async function handleAutoRuleConfirmChange() {
    const {confirm} = getAutoRuleModalElements();
    if (!confirm || !confirm.checked) {
        return;
    }

    if (isAutoScheduleInProgress) {
        return;
    }

    const selectedRuleIds = getSelectedAutoRuleIds();
    if (selectedRuleIds.length === 0) {
        alert("Please select at least one scheduling rule.");
        confirm.checked = false;
        return;
    }

    isAutoScheduleInProgress = true;
    setAutoRuleConfirmLoadingState(true);

    try {
        await autoScheduleShift(selectedRuleIds);
    } finally {
        isAutoScheduleInProgress = false;
        setAutoRuleConfirmLoadingState(false);
        confirm.checked = false;
    }
}

function initializeAutoScheduleRuleModalEvents() {
    const {modal, selectAll, confirm} = getAutoRuleModalElements();
    if (!modal || !selectAll || !confirm || modal.dataset.eventsBound === "true") {
        return;
    }

    modal.addEventListener("click", closeAutoScheduleRulePopup);
    selectAll.addEventListener("change", handleAutoRuleSelectAllChange);
    confirm.addEventListener("change", () => {
        void handleAutoRuleConfirmChange();
    });
    modal.dataset.eventsBound = "true";
}

async function openAutoScheduleRulePopup() {
    const {modal, confirm, selectAll, list} = getAutoRuleModalElements();
    if (!modal || !confirm || !selectAll || !list) return;

    initializeAutoScheduleRuleModalEvents();
    confirm.checked = false;
    selectAll.checked = true;
    selectAll.indeterminate = false;
    list.textContent = "Loading rules...";

    cachedAutoScheduleRules = await fetchAutoScheduleRules();
    renderAutoScheduleRuleList(cachedAutoScheduleRules);
    setAutoRuleConfirmLoadingState(isAutoScheduleInProgress);
    modal.classList.remove("hidden");
}

function closeAutoScheduleRulePopup() {
    const {modal, confirm} = getAutoRuleModalElements();
    if (!modal) return;
    modal.classList.add("hidden");
    if (confirm) {
        confirm.checked = false;
    }

    if (!isAutoScheduleInProgress) {
        setAutoRuleConfirmLoadingState(false);
    }
}

async function loadPolicies() {
    const input = document.getElementById("minimumStaffInput");
    const localValue = localStorage.getItem("shiftPlannerMinimumStaff");

    if (input) {
        input.value = localValue;
    }

    try {
        const response = await apiFetch("api/policies");
        if (!response.ok) {
            console.error(`Unable to load policies from API. Status: ${response.status}`);
            return;
        }

        const policies = (await response.json()).map(normalizePolicy);
        const minimumStaffPolicy = getPolicyById(policies, MINIMUM_STAFF_POLICY_ID);

        if (minimumStaffPolicy) {
            const value = String(minimumStaffPolicy.param1);
            localStorage.setItem("shiftPlannerMinimumStaff", value);
            if (input) {
                input.value = value;
            }
        }
    } catch (error) {
        console.error("Unable to load policies from API.", error);
    }
}

async function savePolicies() {
    const input = document.getElementById("minimumStaffInput");
    const parsedValue = Number(input?.value);
    const nextValue = Number.isFinite(parsedValue) && parsedValue > 0 ? Math.floor(parsedValue) : 3;

    if (input) {
        input.value = String(nextValue);
    }

    localStorage.setItem("shiftPlannerMinimumStaff", String(nextValue));

    try {
        const response = await apiFetch(`api/policies/${MINIMUM_STAFF_POLICY_ID}`, {
            method: "PUT",
            // OpenAPI contract expects snake_case: param_1
            body: JSON.stringify({param_1: nextValue})
        });

        if (!response.ok) {
            console.error(`Unable to save policies. Status: ${response.status}`);
            alert("Failed to save policy via API. Saved locally only.");
            return;
        }

        const updatedPolicies = (await response.json()).map(normalizePolicy);
        const minimumStaffPolicy = getPolicyById(updatedPolicies, MINIMUM_STAFF_POLICY_ID);
        const storedValue = minimumStaffPolicy ? String(minimumStaffPolicy.param1) : String(nextValue);

        if (input) {
            input.value = storedValue;
        }

        localStorage.setItem("shiftPlannerMinimumStaff", storedValue);
        alert("Policies saved.");
    } catch (error) {
        console.error("Unable to save policies.", error);
        alert("Failed to save policy via API. Saved locally only.");
    }
}

function getTaskColors() {
    return defaultTaskColors;
}

function renderTaskColors() {
    const container = document.getElementById("taskColorList");
    if (!container) return;

    const taskColors = getTaskColors();
    container.innerHTML = "";

    taskNames.forEach(task => {
        const color = taskColors[task] || "#cccccc";

        const row = document.createElement("div");
        row.className = "task-item task-row";
        row.innerHTML = `
      <div class="task-color-name">${task}</div>
      <div class="task-color-controls">
        <span class="task-color-preview" style="background:${color}"></span>
      </div>
    `;
        container.appendChild(row);
    });
}

function applyTaskColors() {
    const taskColors = getTaskColors();
    const root = document.documentElement;

    root.style.setProperty("--desk", taskColors["Desk"]);
    root.style.setProperty("--checkin", taskColors["Check-in"]);
    root.style.setProperty("--picking", taskColors["Picking"]);
    root.style.setProperty("--shelving", taskColors["Shelving"]);
    root.style.setProperty("--meeting", taskColors["Meeting"]);
    root.style.setProperty("--lunch", taskColors["Lunch"]);
    root.style.setProperty("--event", taskColors["Event"]);
    root.style.setProperty("--eventprep", taskColors["Event Prep"]);
    root.style.setProperty("--closing-15min", taskColors["Closing-15mins"]);
    root.style.setProperty("--training", taskColors["Training"]);
    root.style.setProperty("--block", taskColors["Block"]);
    root.style.setProperty("--bell", taskColors["Bell"]);
    root.style.setProperty("--roaming", taskColors["Roaming"]);
    root.style.setProperty("--lunchcheckin", taskColors["Lunch/Check-in"]);
    root.style.setProperty("--lunchbell", taskColors["Lunch/Bell"]);
    root.style.setProperty("--lunchroaming", taskColors["Lunch/Roaming"]);
    root.style.setProperty("--optional", taskColors["Optional"]);
    root.style.setProperty("--offsite", taskColors["Off-site"]);
}

function refreshReviewBadges() {
    if (document.getElementById("reviewCalendarBody")) {
        renderHeaderRow("reviewHeaderRow");
        buildCalendar("reviewCalendarBody", false);
    }
}

/* ---------------- SCHEDULE ---------------- */

function createDefaultSchedule() {
    const staffColumns = Math.max(getActiveShiftStaff().length, 1);
    const schedule = [];

    for (let row = 0; row < timeSlots.length; row++) {
        const rowItems = [];
        for (let col = 0; col < staffColumns; col++) {
            rowItems.push("Optional");
        }
        schedule.push(rowItems);
    }
    return schedule;
}

function normalizeScheduleToStaffCount() {
    const staffCount = Math.max(getActiveShiftStaff().length, 1);
    let schedule = getSchedule();

    schedule = schedule.map(row => {
        const updatedRow = [...row];

        while (updatedRow.length < staffCount) {
            updatedRow.push("Optional");
        }

        if (updatedRow.length > staffCount) {
            updatedRow.length = staffCount;
        }

        return updatedRow;
    });

    saveSchedule(schedule);
    return schedule;
}

function getSchedule() {
    return JSON.parse(localStorage.getItem("shiftPlannerSchedule")) || createDefaultSchedule();
}

function saveSchedule(schedule) {
    localStorage.setItem("shiftPlannerSchedule", JSON.stringify(schedule));
}

function getTaskClass(taskName) {
    return taskName.toLowerCase().replace(/[^a-z0-9]+/g, "");
}

function renderHeaderRow(headerRowId) {
    const headerRow = document.getElementById(headerRowId);
    if (!headerRow) return;

    const activeStaff = getActiveShiftStaff();
    const allStaff = getStaff();
    const isShiftHeader = headerRowId === "shiftHeaderRow";

    headerRow.innerHTML = "";

    if (isShiftHeader) {
        const firstCell = document.createElement("th");
        firstCell.className = "time-cell";
        const frame = document.createElement("div");
        frame.className = "time-cell-frame";
        frame.innerHTML = `<button type="button" class="btn btn-primary" onclick="addStaffColumn()">Add staff</button>`;
        firstCell.appendChild(frame);
        headerRow.appendChild(firstCell);
    } else {
        const firstCell = document.createElement("th");
        firstCell.className = "time-cell";
        const frame = document.createElement("div");
        frame.className = "time-cell-frame";
        frame.textContent = "Time";
        firstCell.appendChild(frame);
        headerRow.appendChild(firstCell);
    }

    if (activeStaff.length === 0) {
        return;
    }

    activeStaff.forEach((staff, index) => {
        const th = document.createElement("th");

        if (isShiftHeader) {
            const options = allStaff.map(optionStaff => {
                const selected = optionStaff.id === staff.id ? "selected" : "";
                return `<option value="${optionStaff.id}" ${selected}>${optionStaff.name}</option>`;
            }).join("");

            th.innerHTML = `
        <select class="staff-name-select" onchange="updateStaffColumn(${index}, this.value)">
          ${options}
        </select>
      `;
        } else {
            th.innerHTML = `
        <div class="staff-header">
          <div class="staff-header-name">${staff.name}</div>
        </div>
      `;
        }

        headerRow.appendChild(th);
    });
}

function rescheduleShift() {
    const confirmed = confirm("Redo the shift and reset all tasks to Optional?");
    if (!confirmed) return;

    saveRemovedStaffIds([]);
    saveDynamicStaffIds([]);
    const newSchedule = createDefaultSchedule();
    saveSchedule(newSchedule);
    buildVisibleCalendars();
    populateDutyDropdowns();
}

function resetShift() {
    rescheduleShift();
}

async function autoScheduleShift(selectedPolicyIds = [MINIMUM_STAFF_POLICY_ID]) {
    const date = getStoredShiftDate();
    if (!date) {
        alert("Please select a date before auto-scheduling.");
        return;
    }

    const validPolicyIds = (Array.isArray(selectedPolicyIds) ? selectedPolicyIds : [MINIMUM_STAFF_POLICY_ID])
        .map(policyId => Number(policyId))
        .filter(Number.isFinite);

    if (validPolicyIds.length === 0) {
        alert("Please select at least one scheduling rule.");
        return;
    }

    try {
        const payload = buildScheduleApiPayload(date, validPolicyIds);
        const response = await apiFetch(`/api/schedules/${encodeURIComponent(date)}?ruleCount=${encodeURIComponent(validPolicyIds.length)}`, {
            method: "POST",
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            let reason = `Failed to auto-schedule shift: ${response.status}`;
            try {
                const errorBody = await response.json();
                if (errorBody?.reason) {
                    reason = errorBody.reason;
                }
            } catch (error) {
                // Keep fallback error message when response body is empty or non-JSON.
            }

            alert(reason);
            return;
        }

        const apiSchedule = await response.json();
        mapApiScheduleToLocalState(apiSchedule);
        updateScheduleTitles();
        buildVisibleCalendars();
        loadNotes("shiftNotes");
        populateDutyDropdowns();
        closeAutoScheduleRulePopup();

        alert(`Shift auto-scheduled for ${date}.`);
    } catch (error) {
        console.error("Unable to auto-schedule shift via API.", error);
        alert("Unable to auto-schedule shift. Please try again.");
    }
}

function updateDropdownColor(select, taskName) {
    taskNames.forEach(task => {
        select.classList.remove(getTaskClass(task));
    });
    select.classList.add(getTaskClass(taskName));
}

function createTaskDropdown(rowIndex, colIndex, selectedTask) {
    const select = document.createElement("select");
    select.className = "task-select";
    select.setAttribute(
        "aria-label",
        `Assign task for column ${colIndex + 1} at ${timeSlots[rowIndex]}`
    );
    select.dataset.row = rowIndex;
    select.dataset.col = colIndex;

    const isFivePmSlot = timeSlots[rowIndex] === "5:00 PM";
    const allowedTasks = isFivePmSlot
        ? ["Closing-15mins", "Off-site"]
        : taskNames;

    const safeSelectedTask = allowedTasks.includes(selectedTask)
        ? selectedTask
        : allowedTasks[0];

    allowedTasks.forEach(task => {
        const option = document.createElement("option");
        option.value = task;
        option.textContent = task;
        if (task === safeSelectedTask) {
            option.selected = true;
        }
        select.appendChild(option);
    });

    // If existing stored value was invalid for 5 PM, normalize it immediately
    if (safeSelectedTask !== selectedTask) {
        const schedule = normalizeScheduleToStaffCount();
        schedule[rowIndex][colIndex] = safeSelectedTask;
        saveSchedule(schedule);
    }

    updateDropdownColor(select, safeSelectedTask);

    select.addEventListener("change", function () {
        assignTaskToSlot(rowIndex, colIndex, this.value, this);
    });

    return select;
}

function buildCalendar(tbodyId, useDropdown = false) {
    const tbody = document.getElementById(tbodyId);
    if (!tbody) return;

    const schedule = normalizeScheduleToStaffCount();
    tbody.innerHTML = "";

    timeSlots.forEach((time, rowIndex) => {
        const tr = document.createElement("tr");

        const timeTd = document.createElement("td");
        timeTd.className = "time-cell";
        const timeFrame = document.createElement("div");
        timeFrame.className = "time-cell-frame";
        timeFrame.textContent = time;
        timeTd.appendChild(timeFrame);
        tr.appendChild(timeTd);

        for (let col = 0; col < schedule[rowIndex].length; col++) {
            const taskName = schedule[rowIndex][col];
            const taskClass = getTaskClass(taskName);
            const td = document.createElement("td");
            td.className = "task-cell";

            if (useDropdown) {
                td.appendChild(createTaskDropdown(rowIndex, col, taskName));
            } else {
                td.innerHTML = `<div class="task-badge ${taskClass}">${taskName}</div>`;
            }

            tr.appendChild(td);
        }

        tbody.appendChild(tr);
    });
}

function assignTaskToSlot(rowIndex, colIndex, taskName, selectElement = null) {
    const schedule = normalizeScheduleToStaffCount();
    schedule[rowIndex][colIndex] = taskName;
    saveSchedule(schedule);

    if (selectElement) {
        updateDropdownColor(selectElement, taskName);
    }

    refreshReviewBadges();
}

const taskNameToApiTaskId = {
    "Desk": 1,
    "Check-in": 2,
    "Picking": 3,
    "Shelving": 4,
    "Meeting": 5,
    "Lunch": 6,
    "Event": 7,
    "Event Prep": 8,
    "Closing-15mins": 9,
    "Training": 10,
    "Block": 11,
    "Bell": 12,
    "Roaming": 13,
    "Lunch/Check-in": 14,
    "Lunch/Bell": 15,
    "Lunch/Roaming": 16,
    "Optional": 17,
    "Off-site": 18
};

function toApiTimeSlot(uiTimeSlot) {
    const match = /^(\d{1,2}):(\d{2})\s*(AM|PM)$/i.exec(uiTimeSlot || "");
    if (!match) {
        return null;
    }

    let hours = Number(match[1]);
    const minutes = Number(match[2]);
    const meridiem = match[3].toUpperCase();

    if (meridiem === "PM" && hours !== 12) {
        hours += 12;
    }
    if (meridiem === "AM" && hours === 12) {
        hours = 0;
    }

    return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}`;
}

function getStaffIdByName(staffName) {
    if (!staffName) {
        return null;
    }

    const staff = getStaff().find(item => item.name === staffName);
    return staff ? Number(staff.id) : null;
}

function buildScheduleAssignmentsPayload(schedule, activeStaff) {
    const assignments = [];

    schedule.forEach((row, rowIndex) => {
        const timeSlot = toApiTimeSlot(timeSlots[rowIndex]);
        if (!timeSlot) {
            return;
        }

        row.forEach((taskName, colIndex) => {
            const staff = activeStaff[colIndex];
            const taskId = taskNameToApiTaskId[taskName] || taskNameToApiTaskId.Optional;
            if (!staff || !taskId) {
                return;
            }

            assignments.push({
                staffId: Number(staff.id),
                timeSlot,
                taskId
            });
        });
    });

    return assignments;
}

function resolveScheduleStaffColumns(schedule) {
    const activeStaff = getActiveShiftStaff();
    if (activeStaff.length > 0) {
        return activeStaff;
    }

    const allStaff = getStaff();
    const columnCount = Array.isArray(schedule?.[0]) ? schedule[0].length : 0;
    return allStaff.slice(0, Math.max(columnCount, 1));
}

function buildScheduleApiPayload(date, policyIds = [MINIMUM_STAFF_POLICY_ID]) {
    const schedule = normalizeScheduleToStaffCount();
    const activeStaff = resolveScheduleStaffColumns(schedule);
    if (activeStaff.length === 0) {
        throw new Error("No active staff available to save this schedule.");
    }

    const fallbackStaffId = Number(activeStaff[0].id);
    const dailyAssignments = JSON.parse(localStorage.getItem("shiftPlannerDailyAssignments")) || {};
    const notes = document.getElementById("shiftNotes")?.value || "";

    const normalizedPolicyIds = (Array.isArray(policyIds) ? policyIds : [MINIMUM_STAFF_POLICY_ID])
        .map(policyId => Number(policyId))
        .filter(Number.isFinite);

    return {
        date,
        rosterStaffId: getStaffIdByName(dailyAssignments.roster) ?? fallbackStaffId,
        bankingStaffId: getStaffIdByName(dailyAssignments.banking) ?? fallbackStaffId,
        backupStaffId: getStaffIdByName(dailyAssignments.bankingBackup) ?? fallbackStaffId,
        inspectionStaffId: getStaffIdByName(dailyAssignments.inspection) ?? fallbackStaffId,
        notes,
        policies: normalizedPolicyIds.length > 0 ? normalizedPolicyIds : [MINIMUM_STAFF_POLICY_ID],
        assignments: buildScheduleAssignmentsPayload(schedule, activeStaff)
    };
}

async function saveScheduleToApi(date) {
    const payload = buildScheduleApiPayload(date);
    const response = await apiFetch(`/api/schedules/${encodeURIComponent(date)}`, {
        method: "PUT",
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        let reason = `Failed to save schedule: ${response.status}`;
        try {
            const errorBody = await response.json();
            if (errorBody?.reason) {
                reason = errorBody.reason;
            }
        } catch (error) {
            // Keep fallback error message when response body is empty or non-JSON.
        }
        throw new Error(reason);
    }

    return response.json();
}

function reviewSchedule() {

    window.location.href = "review.html";
}

function openSaveToDatePicker() {
    const saveToDateInput = document.getElementById("saveToDate");
    if (!saveToDateInput) return;

    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, "0");
    const dd = String(today.getDate()).padStart(2, "0");
    saveToDateInput.min = `${yyyy}-${mm}-${dd}`;
    saveToDateInput.value = "";

    saveToDateInput.focus();

    if (typeof saveToDateInput.showPicker === "function") {
        saveToDateInput.showPicker();
    } else {
        saveToDateInput.click();
    }
}

async function saveShiftToSelectedDate() {
    const saveToDateInput = document.getElementById("saveToDate");
    if (!saveToDateInput || !saveToDateInput.value) return;

    localStorage.setItem("shiftPlannerShiftDate", saveToDateInput.value);

    const shiftDateInput = document.getElementById("shiftDate");
    if (shiftDateInput) {
        shiftDateInput.value = saveToDateInput.value;
    }

    updateScheduleTitles();

    try {
        await saveScheduleToApi(saveToDateInput.value);
        alert(`Shift saved to ${saveToDateInput.value}.`);
    } catch (error) {
        console.error("Unable to save shift via API.", error);
        alert(error?.message || `Failed to save shift to ${saveToDateInput.value}.`);
    }
}

function exportShiftScreenAsPDF() {
    window.print();
}

function buildVisibleCalendars() {
    if (document.getElementById("shiftCalendarBody")) {
        renderHeaderRow("shiftHeaderRow");
        buildCalendar("shiftCalendarBody", true);
    }

    if (document.getElementById("reviewCalendarBody")) {
        renderHeaderRow("reviewHeaderRow");
        buildCalendar("reviewCalendarBody", false);
    }
}

function refreshAllHeaders() {
    buildVisibleCalendars();
}

/* ---------------- REVIEW PAGE BUTTONS ---------------- */

function setupReviewPageButtons() {
    const editButton = document.getElementById("reviewEditScheduleBtn");
    const backButton = document.getElementById("reviewBackBtn");

    if (getCurrentRole() === "Manager") {
        if (editButton) {
            editButton.style.display = "none";
        }
        if (backButton) {
            backButton.textContent = "Back";
            backButton.onclick = () => {
                window.location.href = "manager.html";
            };
        }
    } else {
        if (editButton) {
            editButton.style.display = "inline-block";
        }
        if (backButton) {
            backButton.textContent = "Logout";
            backButton.onclick = () => {
                logout();
            };
        }
    }
}

/* ---------------- NOTES ---------------- */

function saveNotes(elementId, storageKey) {
    const value = document.getElementById(elementId)?.value || "";
    localStorage.setItem(storageKey, value);

}

function loadNotes(elementId, storageKey) {
    const el = document.getElementById(elementId);
    if (el) {
        el.value = localStorage.getItem(storageKey) || "";
    }
}

/* ---------------- DAILY ASSIGNMENTS ---------------- */

function populateDutyDropdown(dropdownId, selectedValue) {
    const dropdown = document.getElementById(dropdownId);
    if (!dropdown) return;

    const activeStaff = getActiveShiftStaff();
    dropdown.innerHTML = `<option value="">Select staff</option>`;

    activeStaff.forEach(staff => {
        const option = document.createElement("option");
        option.value = staff.name;
        option.textContent = staff.name;
        if (staff.name === selectedValue) {
            option.selected = true;
        }
        dropdown.appendChild(option);
    });
}

function populateDutyDropdowns() {
    const saved = JSON.parse(localStorage.getItem("shiftPlannerDailyAssignments")) || {};

    populateDutyDropdown("dutyRoster", saved.roster || "");
    populateDutyDropdown("dutyBanking", saved.banking || "");
    populateDutyDropdown("dutyBankingBackup", saved.bankingBackup || "");
    populateDutyDropdown("dutyInspection", saved.inspection || "");
}

function saveDailyAssignments() {
    const data = {
        roster: document.getElementById("dutyRoster")?.value || "",
        banking: document.getElementById("dutyBanking")?.value || "",
        bankingBackup: document.getElementById("dutyBankingBackup")?.value || "",
        inspection: document.getElementById("dutyInspection")?.value || ""
    };
    localStorage.setItem("shiftPlannerDailyAssignments", JSON.stringify(data));
}

function loadDailyAssignmentsForLibrarian() {
    populateDutyDropdowns();
}

function loadDailyAssignmentsForReview() {
    const data = JSON.parse(localStorage.getItem("shiftPlannerDailyAssignments")) || {};
    const roster = document.getElementById("reviewRosterName");
    const banking = document.getElementById("reviewBankingName");
    const backup = document.getElementById("reviewBankingBackupName");
    const inspection = document.getElementById("reviewInspectionName");

    if (roster) roster.textContent = data.roster || "";
    if (banking) banking.textContent = data.banking || "";
    if (backup) backup.textContent = data.bankingBackup || "";
    if (inspection) inspection.textContent = data.inspection || "";
}

/* ---------------- PAGE INIT ---------------- */

async function initManagerPage() {
    initializeStorage();
    applyTaskColors();
    await fetchAndCacheStaff();
    renderStaffTable();
    await loadPolicies();
    renderTaskColors();
}

async function initLibrarianPage() {
    initializeStorage();
    applyTaskColors();
    initializeAutoScheduleRuleModalEvents();
    await fetchAndCacheStaff();
    buildVisibleCalendars();
    loadNotes("shiftNotes");
    loadDailyAssignmentsForLibrarian();

    const shiftDateInput = document.getElementById("shiftDate");
    const todayString = getTodayDateString();
    if (shiftDateInput) {
        shiftDateInput.value = todayString;
    }
    localStorage.setItem("shiftPlannerShiftDate", todayString);

    const saveToDateInput = document.getElementById("saveToDate");
    if (saveToDateInput) {

        saveToDateInput.min = todayString;
        saveToDateInput.value = todayString;
    }

    await loadShiftDatePicker();
}

async function initReviewPage() {
    initializeStorage();
    applyTaskColors();
    setupReviewDatePickerVisibility();
    loadReviewDatePicker();

    if (isManagerRole()) {
        // Manager path is API-first: do not render local schedule content before fetch.
        await loadReviewScheduleByPickedDate(false);
    } else {
        // Senior librarian/librarian path is local-only.
        buildVisibleCalendars();
        loadNotes("reviewNotes");
        loadDailyAssignmentsForReview();
    }

    setupReviewPageButtons();
}

/* ---------------- GENERIC ---------------- */
