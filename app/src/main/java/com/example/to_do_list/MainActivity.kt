package com.example.to_do_list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

private val AppColors = darkColorScheme(
    primary = Color(0xFFB8A5FF),
    onPrimary = Color(0xFF24164D),
    secondary = Color(0xFFC2B6F5),
    background = Color(0xFF101016),
    onBackground = Color(0xFFF5F2FF),
    surface = Color(0xFF1B1A23),
    onSurface = Color(0xFFF5F2FF),
    surfaceVariant = Color(0xFF282631),
    onSurfaceVariant = Color(0xFFB7B2C5),
    error = Color(0xFFFF8A8A)
)

private val Accent = Color(0xFF9A7BFF)
private val MutedText = Color(0xFFA6A1B3)
private val Categories = listOf("Personal", "College", "Work")
private val Priorities = listOf("High", "Medium", "Low")
private val StatusFilters = listOf("All", "Pending", "Completed", "Overdue")
private const val MILLIS_PER_DAY = 86_400_000L

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = TaskDatabase.getInstance(applicationContext)
        val repository = TaskRepository(database.taskDao())
        setContent {
            MaterialTheme(colorScheme = AppColors) {
                TodoScreen(repository)
            }
        }
    }
}

@Composable
private fun TodoScreen(repository: TaskRepository) {
    val tasks by repository.tasks.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") }
    var categoryFilter by remember { mutableStateOf<String?>(null) }
    var priorityFilter by remember { mutableStateOf<String?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    val completedCount = tasks.count { it.isCompleted }
    val pendingCount = tasks.size - completedCount
    val progress = if (tasks.isEmpty()) 0f else completedCount.toFloat() / tasks.size
    val today = todayEpochDay()
    val visibleTasks = TaskRules.filter(
        tasks = tasks,
        query = searchQuery,
        category = categoryFilter,
        priority = priorityFilter,
        status = statusFilter,
        todayEpochDay = today
    )

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
    val currentDate = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 22.dp,
                end = 22.dp,
                top = 20.dp + contentPadding.calculateTopPadding(),
                bottom = 24.dp + contentPadding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        greeting,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(currentDate, style = MaterialTheme.typography.bodyLarge, color = MutedText)
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF211D32))
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Your progress",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "$completedCount of ${tasks.size} tasks completed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedText
                                )
                            }
                            Text(
                                "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD0C2FF)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Accent,
                            trackColor = Color(0xFF393346)
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("Total", tasks.size.toString(), Modifier.weight(1f))
                    StatCard("Completed", completedCount.toString(), Modifier.weight(1f))
                    StatCard("Pending", pendingCount.toString(), Modifier.weight(1f))
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "My Tasks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                editingTask = null
                                showEditor = true
                            },
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Text("＋  Add Task", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Search tasks") },
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusFilters.forEach { filter ->
                            FilterChip(
                                selected = statusFilter == filter,
                                onClick = { statusFilter = filter },
                                label = { Text(filter, maxLines = 1) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SelectionMenu(
                            label = categoryFilter ?: "All categories",
                            options = listOf("All categories") + Categories,
                            onSelected = { categoryFilter = it.takeUnless { value -> value == "All categories" } },
                            modifier = Modifier.weight(1f)
                        )
                        SelectionMenu(
                            label = priorityFilter ?: "All priorities",
                            options = listOf("All priorities") + Priorities,
                            onSelected = { priorityFilter = it.takeUnless { value -> value == "All priorities" } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (visibleTasks.isEmpty()) {
                item {
                    EmptyTasksState(
                        hasTasks = tasks.isNotEmpty(),
                        onAddTask = {
                            editingTask = null
                            showEditor = true
                        }
                    )
                }
            } else {
                items(items = visibleTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        todayEpochDay = today,
                        onCheckedChange = { checked ->
                            scope.launch { repository.setCompleted(task.id, checked) }
                        },
                        onEdit = {
                            editingTask = task
                            showEditor = true
                        },
                        onDelete = { taskToDelete = task }
                    )
                }
            }
        }
    }

    if (showEditor) {
        TaskEditorDialog(
            task = editingTask,
            onDismiss = { showEditor = false },
            onSave = { updated ->
                scope.launch {
                    if (updated.id == 0) repository.addTask(updated)
                    else repository.updateTask(updated)
                }
                showEditor = false
            }
        )
    }

    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Delete task?") },
            text = { Text("“${task.title}” will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repository.deleteTask(task.id) }
                    taskToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(value, color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                label,
                color = MutedText,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SelectionMenu(
    label: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        TextButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyTasksState(hasTasks: Boolean, onAddTask: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(if (hasTasks) "⌕" else "✦", color = Accent, fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                if (hasTasks) "No matching tasks" else "A clear mind starts here",
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (hasTasks) "Try adjusting your search or filters."
                else "Add your first task and make today count.",
                color = MutedText,
                style = MaterialTheme.typography.bodyMedium
            )
            if (!hasTasks) {
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onAddTask) { Text("Create a task") }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    todayEpochDay: Long,
    onCheckedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val overdue = TaskRules.isOverdue(task, todayEpochDay)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(19.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = task.isCompleted, onCheckedChange = onCheckedChange)
                Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                    Text(
                        task.title,
                        color = if (task.isCompleted) MutedText else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Spacer(Modifier.height(7.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        MetadataPill(task.category, Accent)
                        MetadataPill(
                            "${task.priority} priority",
                            when (task.priority) {
                                "High" -> Color(0xFFFF8A8A)
                                "Low" -> Color(0xFF8FD8B0)
                                else -> Color(0xFFFFCF75)
                            }
                        )
                    }
                }
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MutedText, fontSize = 12.sp)
                }
            }
            task.dueDateEpochDay?.let { dueDate ->
                Text(
                    text = when {
                        overdue -> "Overdue · ${formatEpochDay(dueDate)}"
                        dueDate == todayEpochDay -> "Due today"
                        dueDate == todayEpochDay + 1 -> "Due tomorrow"
                        else -> "Due ${formatEpochDay(dueDate)}"
                    },
                    modifier = Modifier.padding(start = 58.dp, top = 2.dp),
                    color = if (overdue) MaterialTheme.colorScheme.error else MutedText,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun MetadataPill(text: String, tint: Color) {
    Text(
        text,
        modifier = Modifier
            .padding(vertical = 1.dp),
        color = tint,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1
    )
}

@Composable
private fun TaskEditorDialog(
    task: Task?,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    var title by remember(task?.id) { mutableStateOf(task?.title.orEmpty()) }
    var category by remember(task?.id) { mutableStateOf(task?.category ?: "Personal") }
    var priority by remember(task?.id) { mutableStateOf(task?.priority ?: "Medium") }
    var dueDateEpochDay by remember(task?.id) { mutableStateOf(task?.dueDateEpochDay) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dueDateEpochDay?.times(MILLIS_PER_DAY)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (task == null) "New task" else "Edit task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Task name") },
                    singleLine = true,
                    isError = title.isNotEmpty() && title.isBlank(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                Text("Category", style = MaterialTheme.typography.labelLarge)
                ChoiceRow(options = Categories, selected = category, onSelected = { category = it })
                Text("Priority", style = MaterialTheme.typography.labelLarge)
                ChoiceRow(options = Priorities, selected = priority, onSelected = { priority = it })
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(dueDateEpochDay?.let { formatEpochDay(it) } ?: "No due date", color = MutedText)
                    Row {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text(if (dueDateEpochDay == null) "Set date" else "Change")
                        }
                        if (dueDateEpochDay != null) {
                            TextButton(onClick = { dueDateEpochDay = null }) { Text("Clear") }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    val base = task ?: Task(title = title.trim())
                    onSave(
                        base.copy(
                            title = title.trim(),
                            category = category,
                            priority = priority,
                            dueDateEpochDay = dueDateEpochDay
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDateEpochDay = datePickerState.selectedDateMillis?.div(MILLIS_PER_DAY)
                    showDatePicker = false
                }) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ChoiceRow(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelected(option) },
                label = { Text(option, maxLines = 1) }
            )
        }
    }
}

private fun todayEpochDay(): Long {
    val local = Calendar.getInstance()
    return GregorianCalendar(
        TimeZone.getTimeZone("UTC"),
        Locale.ROOT
    ).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis / MILLIS_PER_DAY
}

private fun formatEpochDay(epochDay: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(epochDay * MILLIS_PER_DAY))
