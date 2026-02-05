package com.behealthy.app.feature.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.behealthy.app.ui.theme.ThemeStyle
import com.behealthy.app.ui.theme.getThemeColorScheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var isEditing by remember { 
        mutableStateOf(false) 
    }
    
    // Avatar Picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.updateAvatar(context, uri)
        }
    }
    
    // Note Image Picker
    val noteImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            viewModel.updateNoteImage(it.toString())
        }
    }
    
    // Date Picker
    var showDatePicker by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLogDialog by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    if (showLogDialog) {
        LogViewerDialog(
            onDismiss = { showLogDialog = false },
            onTriggerSync = { viewModel.triggerSync() }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.themeStyle,
            currentAlpha = uiState.backgroundAlpha,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { viewModel.updateThemeStyle(it) },
            onAlphaChange = { viewModel.updateBackgroundAlpha(it) }
        )
    }
    
    if (showDatePicker) {
        val initialDateMillis = uiState.birthday?.let { 
            try {
                LocalDate.parse(it, dateFormatter).toEpochDay() * 24 * 60 * 60 * 1000 
            } catch (e: Exception) { null }
        }
        
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        viewModel.updateBirthday(date.format(dateFormatter))
                    }
                    showDatePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Auto-save when exiting edit mode
    BackHandler(enabled = isEditing) {
        isEditing = false
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("我的档案", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    if (!isEditing) {
                        IconButton(onClick = { showThemeDialog = true }) {
                            Icon(Icons.Default.Palette, contentDescription = "主题风格")
                        }
                    }
                },
                actions = {
                    if (!isEditing && !uiState.nickname.isNullOrEmpty()) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
                    } else if (isEditing) {
                        IconButton(onClick = { isEditing = false }) {
                            Icon(Icons.Default.Close, contentDescription = "关闭")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Remove the edit button from bottom right
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (isEditing) {
                ProfileEditView(
                    uiState = uiState,
                    viewModel = viewModel,
                    onAvatarClick = { imagePicker.launch("image/*") },
                    onNicknameChange = { viewModel.updateNickname(it) },
                    onNoteChange = { viewModel.updateNote(it) },
                    onNoteImageClick = { noteImagePicker.launch("image/*") },
                    onBirthdayClick = { showDatePicker = true },
                    onBirthdayReminderChange = { viewModel.setBirthdayReminderEnabled(it) },
                    onAvatarCropChange = { viewModel.setAvatarCropEnabled(it) }
                )
            } else {
                ProfileDisplayView(uiState = uiState, onEditClick = { isEditing = true })
            }
        }
    }
}

@Composable
fun ProfileDisplayView(uiState: ProfileUiState, onEditClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header: Avatar & Name
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onEditClick)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.avatarUri.isNullOrEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "头像",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(uiState.avatarUri),
                        contentDescription = "头像",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = uiState.nickname ?: "未命名",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "点击编辑资料",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        if (!uiState.note.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        
        if (!uiState.noteImageUri.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = rememberAsyncImagePainter(uiState.noteImageUri),
                contentDescription = "个人简介图片",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(number = uiState.totalWorkoutDays.toString(), label = "健身天数")
            StatItem(number = uiState.totalMoodRecords.toString(), label = "心情记录")
            StatItem(number = uiState.currentStreak.toString(), label = "连续打卡")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Achievements
        Text(
            text = "我的成就",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        AchievementBadges(uiState)
        
        Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
    }
}

@Composable
fun ProfileEditView(
    uiState: ProfileUiState,
    viewModel: ProfileViewModel,
    onAvatarClick: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onNoteImageClick: () -> Unit,
    onBirthdayClick: () -> Unit,
    onBirthdayReminderChange: (Boolean) -> Unit,
    onAvatarCropChange: (Boolean) -> Unit
) {
    // Use local state for inputs to prevent cursor jumping (Item 15)
    var nickname by remember { mutableStateOf(uiState.nickname ?: "") }
    var note by remember { mutableStateOf(uiState.note ?: "") }
    
    // Update local state when uiState changes, but preserve user input
    LaunchedEffect(uiState.nickname) {
        if (nickname.isEmpty() && !uiState.nickname.isNullOrEmpty()) {
            nickname = uiState.nickname
        }
    }
    LaunchedEffect(uiState.note) {
        if (note.isEmpty() && !uiState.note.isNullOrEmpty()) {
            note = uiState.note
        }
    }

    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.themeStyle,
            currentAlpha = uiState.backgroundAlpha,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { 
                viewModel.updateThemeStyle(it)
                // Dialog stays open to allow adjusting multiple things? 
                // Or close it? Original code closed it.
                // showThemeDialog = false // Let user close it manually if they want to adjust slider?
                // Actually, selecting a theme usually closes dialog.
                // But transparency slider needs dialog to stay open.
                // So let's NOT close on theme select, only on Dismiss/Close button.
            },
            onAlphaChange = { viewModel.updateBackgroundAlpha(it) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "编辑个人资料",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable { onAvatarClick() }
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.avatarUri.isNullOrEmpty()) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(50.dp))
            } else {
                Image(
                    painter = rememberAsyncImagePainter(uiState.avatarUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Nickname
        OutlinedTextField(
            value = nickname,
            onValueChange = { 
                nickname = it
                onNicknameChange(it) 
            },
            label = { Text("昵称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = uiState.birthday ?: "",
            onValueChange = {},
            label = { Text("生日") },
            leadingIcon = { Icon(Icons.Default.DateRange, null) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBirthdayClick),
            enabled = false, // Disable typing, but handle click on parent or overlay
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        )
        // Hack to make read-only text field clickable
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // Approximate height of TextField
                .offset(y = (-56).dp)
                .clickable(onClick = onBirthdayClick)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = uiState.note ?: "",
            onValueChange = onNoteChange,
            label = { Text("个人简介 (支持Emoji)") },
            leadingIcon = { Icon(Icons.Default.Edit, null) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Note Image Picker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onNoteImageClick),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.noteImageUri.isNullOrEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("添加个人简介图片", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Image(
                    painter = rememberAsyncImagePainter(uiState.noteImageUri),
                    contentDescription = "个人简介图片",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("生日提醒", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = uiState.birthdayReminderEnabled, onCheckedChange = onBirthdayReminderChange)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("头像裁剪", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = uiState.avatarCropEnabled, onCheckedChange = onAvatarCropChange)
                }
                // Theme configuration removed - now accessible from main profile view
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Auto-save hint
        Text(
            text = "资料将自动保存",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StatItem(number: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AchievementBadges(uiState: ProfileUiState) {
    val badges = listOf(
        Triple("初次体验", uiState.hasFirstWorkoutBadge, Icons.Default.Star),
        Triple("七日连胜", uiState.hasSevenDayStreakBadge, Icons.Default.ThumbUp),
        Triple("月度达人", uiState.hasThirtyDayStreakBadge, Icons.Default.Favorite),
        Triple("百炼成钢", uiState.hasHundredWorkoutsBadge, Icons.Default.Face),
        Triple("积极向上", uiState.hasPositiveWeekBadge, Icons.Default.CheckCircle),
        Triple("心情管家", uiState.hasMoodMonthBadge, Icons.Default.DateRange)
    )
    
    // Using Column/Row instead of LazyVerticalGrid to avoid nested scrolling with parent Scroll
    Column {
        val rows = badges.chunked(3)
        rows.forEach { rowBadges ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowBadges.forEach { (name, unlocked, icon) ->
                    BadgeItem(name, unlocked, icon)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BadgeItem(name: String, unlocked: Boolean, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (unlocked) MaterialTheme.colorScheme.primaryContainer 
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    currentAlpha: Float,
    onDismiss: () -> Unit,
    onThemeSelected: (String) -> Unit,
    onAlphaChange: (Float) -> Unit
) {
    val themes = ThemeStyle.values()
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().heightIn(max = 700.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "主题风格设置",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Transparency Slider
                Text(
                    text = "背景透明度: ${(currentAlpha * 100).toInt()}%", 
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = currentAlpha,
                    onValueChange = onAlphaChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Theme Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(themes) { theme ->
                        ThemeCard(
                            theme = theme,
                            isSelected = theme.name == currentTheme,
                            onClick = { onThemeSelected(theme.name) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("完成")
                }
            }
        }
    }
}

@Composable
fun ThemeCard(theme: ThemeStyle, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            // Preview Background
            com.behealthy.app.ui.DynamicThemeBackground(theme = theme, alpha = 1f)
            
            if (isSelected) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = getThemeName(theme.name),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

fun getThemeName(themeStyleName: String): String {
    return try {
        when (ThemeStyle.valueOf(themeStyleName)) {
            ThemeStyle.Default -> "默认风格"
            ThemeStyle.Tech -> "科技风格"
            ThemeStyle.Sports -> "运动风格"
            ThemeStyle.Cute -> "可爱风格"
            ThemeStyle.Doraemon -> "哆啦A梦"
            ThemeStyle.Minions -> "小黄人"
            ThemeStyle.WallE -> "机器人总动员"
            ThemeStyle.NewYear -> "春节过年"
            ThemeStyle.NBA -> "NBA"
            ThemeStyle.Badminton -> "羽毛球"
            ThemeStyle.FootballWorldCup -> "世界杯"
            ThemeStyle.Zen -> "禅"
            ThemeStyle.Dao -> "道"
        }
    } catch (e: Exception) {
        "默认风格"
    }
}
