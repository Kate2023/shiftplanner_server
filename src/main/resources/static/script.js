let currentRole = "Manager";

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
  "Block": "#444444",
  "Bell": "#ff8aa1",
  "Roaming": "#6ed3ff",
  "Lunch/Check-in": "#f7b267",
  "Lunch/Bell": "#ffb3c7",
  "Lunch/Roaming": "#8ee3ef",
  "Optional": "#c7d2e2"
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
  "Optional"
];

const timeSlots = [
  "9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
  "1:00 PM", "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM"
];

const defaultStaff = [
  { id: 1, name: "Emma Li", title: "Senior Librarian" },
  { id: 2, name: "Noah Patel", title: "Librarian" },
  { id: 3, name: "Olivia Chen", title: "Library Assistant" },
  { id: 4, name: "Lucas Singh", title: "Librarian" },
  { id: 5, name: "Ava Wilson", title: "Librarian" },
  { id: 6, name: "Ethan Brown", title: "Library Assistant" },
  { id: 7, name: "Sophia Kumar", title: "Librarian" }
];

const defaultShiftDate = "2026-06-10";

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

  if (!localStorage.getItem("shiftPlannerNotes")) {
    localStorage.setItem("shiftPlannerNotes", "");
  }

  if (!localStorage.getItem("shiftPlannerReviewNotes")) {
    localStorage.setItem("shiftPlannerReviewNotes", "");
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
  const username = document.getElementById("username")?.value.trim();
  const password = document.getElementById("password")?.value.trim();
  const error = document.getElementById("loginError");

  const validManager =
    currentRole === "Manager" &&
    username === "Manager" &&
    password === "manager2026";

  const validLibrarian =
    currentRole === "Librarian" &&
    username === "Senior Librarian" &&
    password === "librarian2026";

  if (validManager) {
    localStorage.setItem("shiftPlannerCurrentRole", "Manager");
    window.location.href = "manager.html";
  } else if (validLibrarian) {
    localStorage.setItem("shiftPlannerCurrentRole", "Librarian");
    window.location.href = "librarian.html";
  } else if (error) {
    error.style.display = "block";
  }
}

function getCurrentRole() {
  return localStorage.getItem("shiftPlannerCurrentRole") || "Manager";
}

function logout() {
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

function loadShiftDatePicker() {
  const shiftDateInput = document.getElementById("shiftDate");
  if (shiftDateInput) {
    shiftDateInput.value = getStoredShiftDate();
  }

  const saveToDateInput = document.getElementById("saveToDate");
  if (saveToDateInput) {
   const today = new Date();
   const yyyy = today.getFullYear();
   const mm = String(today.getMonth() + 1).padStart(2, "0");
   const dd = String(today.getDate()).padStart(2, "0");
   const todayString = `${yyyy}-${mm}-${dd}`;

   saveToDateInput.min = todayString;
   saveToDateInput.value = todayString;
  }

  updateScheduleTitles();
}

function loadReviewDatePicker() {
  const reviewDateInput = document.getElementById("reviewDate");
  if (reviewDateInput) {
    reviewDateInput.value = getStoredShiftDate();
  }
  updateScheduleTitles();
}

function saveShiftDate() {
  const shiftDateInput = document.getElementById("shiftDate");
  if (!shiftDateInput) return;

  localStorage.setItem("shiftPlannerShiftDate", shiftDateInput.value || defaultShiftDate);
  updateScheduleTitles();
}

function syncReviewDateToStoredValue() {
  const reviewDateInput = document.getElementById("reviewDate");
  if (!reviewDateInput) return;

  localStorage.setItem("shiftPlannerShiftDate", reviewDateInput.value || defaultShiftDate);
  updateScheduleTitles();
}

/* ---------------- STAFF ---------------- */

function getStaff() {
  return JSON.parse(localStorage.getItem("shiftPlannerStaff")) || [];
}

function saveStaffList(staffList) {
  localStorage.setItem("shiftPlannerStaff", JSON.stringify(staffList));
}

function getRemovedStaffIds() {
  return JSON.parse(localStorage.getItem("shiftPlannerRemovedStaffIds")) || [];
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
      staff.name.toLowerCase().includes(query) ||
      staff.title.toLowerCase().includes(query)
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
    row.innerHTML = `<td colspan="3">No staff found.</td>`;
    tbody.appendChild(row);
    return;
  }

  staffList.forEach(staff => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${staff.name}</td>
      <td>${staff.title}</td>
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
    formCard.scrollIntoView({ behavior: "smooth" });
  }
}

function resetStaffForm() {
  const title = document.getElementById("staffFormTitle");
  const name = document.getElementById("staffName");
  const staffTitle = document.getElementById("staffTitle");
  const editId = document.getElementById("editingStaffId");

  if (title) title.textContent = "Add Staff Member";
  if (name) name.value = "";
  if (staffTitle) staffTitle.value = "";
  if (editId) editId.value = "";
}

function saveStaff() {
  const id = document.getElementById("editingStaffId")?.value;
  const name = document.getElementById("staffName")?.value.trim();
  const title = document.getElementById("staffTitle")?.value.trim();

  if (!name || !title) {
    alert("Please fill in all staff fields.");
    return;
  }

  let staffList = getStaff();

  if (id) {
    staffList = staffList.map(staff =>
      staff.id == id
        ? { id: staff.id, name, title }
        : staff
    );
  } else {
    const newStaff = {
      id: Date.now(),
      name,
      title
    };
    staffList.push(newStaff);
  }

  saveStaffList(staffList);

  const currentDynamicIds = getDynamicStaffIds().filter(id =>
    staffList.some(staff => staff.id === id)
  );
  saveDynamicStaffIds(currentDynamicIds);

  renderStaffTable();
  resetStaffForm();
  refreshAllHeaders();
  populateDutyDropdowns();
  alert("Staff record saved.");
}

function editStaff(id) {
  const staff = getStaff().find(item => item.id === id);
  if (!staff) return;

  document.getElementById("staffFormTitle").textContent = "Edit Staff Member";
  document.getElementById("staffName").value = staff.name;
  document.getElementById("staffTitle").value = staff.title;
  document.getElementById("editingStaffId").value = staff.id;

  openStaffForm();
}

function deleteStaff(id) {
  const confirmed = confirm("Are you sure you want to delete this staff member?");
  if (!confirmed) return;

  const staffList = getStaff().filter(item => item.id !== id);
  saveStaffList(staffList);

  const dynamicIds = getDynamicStaffIds().filter(staffId => staffId !== id);
  saveDynamicStaffIds(dynamicIds);

  renderStaffTable();
  refreshAllHeaders();
  populateDutyDropdowns();
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

function savePolicies() {
  const value = document.getElementById("minimumStaffInput")?.value;
  localStorage.setItem("shiftPlannerMinimumStaff", value || "3");
  alert("Policies saved.");
}

function loadPolicies() {
  const input = document.getElementById("minimumStaffInput");
  if (input) {
    input.value = localStorage.getItem("shiftPlannerMinimumStaff") || "3";
  }
}

/* ---------------- TASK COLORS ---------------- */

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
}

function refreshDropdownColors() {
  const dropdowns = document.querySelectorAll(".task-select");
  dropdowns.forEach(select => {
    updateDropdownColor(select, select.value);
  });
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
    firstCell.innerHTML = `<button type="button" class="btn btn-primary" onclick="addStaffColumn()">Add staff</button>`;
    headerRow.appendChild(firstCell);
  } else {
    const firstCell = document.createElement("th");
    firstCell.className = "time-cell";
    firstCell.textContent = "Time";
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

function autoScheduleShift() {
  alert("Automatic scheduling will be added later.");
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

  const isSixPmSlot = timeSlots[rowIndex] === "6:00 PM";
  const allowedTasks = isSixPmSlot
    ? ["Closing-15mins", "Block"]
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

  // If existing stored value was invalid for 6 PM, normalize it immediately
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
    timeTd.textContent = time;
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

function saveScheduleData() {
  alert("Schedule saved.");
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
  const todayString = `${yyyy}-${mm}-${dd}`;

  saveToDateInput.min = todayString;
  saveToDateInput.value = todayString;
  saveToDateInput.focus();

  if (typeof saveToDateInput.showPicker === "function") {
    saveToDateInput.showPicker();
  } else {
    saveToDateInput.click();
  }
}

function saveShiftToSelectedDate() {
  const saveToDateInput = document.getElementById("saveToDate");
  if (!saveToDateInput || !saveToDateInput.value) return;

  localStorage.setItem("shiftPlannerShiftDate", saveToDateInput.value);

  const shiftDateInput = document.getElementById("shiftDate");
  if (shiftDateInput) {
    shiftDateInput.value = saveToDateInput.value;
  }

  updateScheduleTitles();
  alert(`Shift saved to ${saveToDateInput.value}.`);
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
  const saveButton = document.getElementById("reviewSaveBtn");

  if (getCurrentRole() === "Manager") {
    if (editButton) {
      editButton.style.display = "none";
    }
    if (saveButton) {
      saveButton.style.display = "none";
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
    if (saveButton) {
      saveButton.style.display = "inline-block";
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

function initManagerPage() {
  initializeStorage();
  applyTaskColors();
  renderStaffTable();
  loadPolicies();
  renderTaskColors();
}

function initLibrarianPage() {
  initializeStorage();
  applyTaskColors();
  buildVisibleCalendars();
  loadNotes("shiftNotes", "shiftPlannerNotes");
  loadDailyAssignmentsForLibrarian();
  loadShiftDatePicker();
}

function initReviewPage() {
  initializeStorage();
  applyTaskColors();
  buildVisibleCalendars();
  loadNotes("reviewNotes", "shiftPlannerReviewNotes");
  loadDailyAssignmentsForReview();
  loadReviewDatePicker();
  setupReviewPageButtons();
}

/* ---------------- GENERIC ---------------- */

function mockSave(message) {
  alert(message);
}