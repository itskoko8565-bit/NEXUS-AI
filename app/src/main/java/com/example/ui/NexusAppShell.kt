package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.model.ProjectFormat
import com.example.data.model.ProjectStage
import com.example.data.model.ScriptFormat
import com.example.data.model.StudioPreferences
import com.example.data.repository.NexusAiRepository
import com.example.data.repository.NexusFirestoreRepository
import com.example.firebase.signOutUser
import com.example.ui.components.StagePill
import com.example.ui.navigation.NavGroup
import com.example.ui.navigation.NexusDestination
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.ClipIntelScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.ResearchScreen
import com.example.ui.screens.ScriptGenScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShortsScreen
import com.example.ui.screens.VideoStudioScreen
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.NexusAiViewModel
import com.example.ui.viewmodel.NexusWorkspaceViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusAppShell(
    currentUserId: String,
    userDisplayName: String,
    userEmail: String,
    settingsViewModel: SettingsViewModel,
    preferences: StudioPreferences
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val workspaceViewModel: NexusWorkspaceViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                NexusWorkspaceViewModel(NexusFirestoreRepository(db), currentUserId)
            }
        }
    )

    val aiViewModel: NexusAiViewModel = viewModel(
        key = "ai_$currentUserId",
        factory = viewModelFactory {
            initializer { NexusAiViewModel(NexusAiRepository()) }
        }
    )

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = NexusDestination.fromRoute(backStackEntry?.destination?.route)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val projectsState by workspaceViewModel.projectsState.collectAsStateWithLifecycle()
    val briefsState by workspaceViewModel.researchBriefsState.collectAsStateWithLifecycle()
    val scriptsState by workspaceViewModel.scriptsState.collectAsStateWithLifecycle()
    val calendarState by workspaceViewModel.calendarState.collectAsStateWithLifecycle()
    val clipAnalysesState by workspaceViewModel.clipAnalysesState.collectAsStateWithLifecycle()

    val researchAiState by aiViewModel.researchState.collectAsStateWithLifecycle()
    val scriptAiState by aiViewModel.scriptStreamState.collectAsStateWithLifecycle()
    val shortsAiState by aiViewModel.shortsState.collectAsStateWithLifecycle()
    val storyboardAiState by aiViewModel.storyboardState.collectAsStateWithLifecycle()
    val clipIntelAiState by aiViewModel.clipIntelState.collectAsStateWithLifecycle()

    val prefilledTopic by aiViewModel.prefilledScriptTopic.collectAsStateWithLifecycle()
    val prefilledAngle by aiViewModel.prefilledScriptAngle.collectAsStateWithLifecycle()

    LaunchedEffect(workspaceViewModel) {
        workspaceViewModel.events.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val navigateTo: (NexusDestination) -> Unit = { dest ->
        navController.navigate(dest.route) {
            popUpTo(NexusDestination.DASHBOARD.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        val isExpandedLayout = maxWidth >= 780.dp

        if (isExpandedLayout) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Permanent Professional Sidebar for Tablets / Foldables
                Surface(
                    modifier = Modifier
                        .width(244.dp)
                        .fillMaxHeight(),
                    color = SlateSurface,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    StudioDrawerContent(
                        currentDest = currentDest,
                        userDisplayName = userDisplayName,
                        onSelect = navigateTo,
                        modifier = Modifier
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    )
                }

                Scaffold(
                    modifier = Modifier.weight(1f),
                    containerColor = ObsidianBg,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = currentDest.title,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = SlateSurface,
                                titleContentColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(modifier = Modifier.widthIn(max = 980.dp)) {
                            NexusNavHost(
                                navController = navController,
                                userDisplayName = userDisplayName,
                                userEmail = userEmail,
                                preferences = preferences,
                                workspaceViewModel = workspaceViewModel,
                                aiViewModel = aiViewModel,
                                settingsViewModel = settingsViewModel,
                                projectsState = projectsState,
                                briefsState = briefsState,
                                scriptsState = scriptsState,
                                calendarState = calendarState,
                                clipAnalysesState = clipAnalysesState,
                                researchAiState = researchAiState,
                                scriptAiState = scriptAiState,
                                shortsAiState = shortsAiState,
                                storyboardAiState = storyboardAiState,
                                clipIntelAiState = clipIntelAiState,
                                prefilledTopic = prefilledTopic,
                                prefilledAngle = prefilledAngle,
                                onNavigate = navigateTo,
                                onSignOut = { signOutUser(credentialManager, scope) }
                            )
                        }
                    }
                }
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = SlateSurface,
                        modifier = Modifier.width(292.dp)
                    ) {
                        StudioDrawerContent(
                            currentDest = currentDest,
                            userDisplayName = userDisplayName,
                            onSelect = { dest ->
                                scope.launch { drawerState.close() }
                                navigateTo(dest)
                            }
                        )
                    }
                }
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = ObsidianBg,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        CenterAlignedTopAppBar(
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("open_studio_drawer_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = "Open NEXUS AI modules menu",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            title = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = currentDest.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = currentDest.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = SlateSurface,
                                titleContentColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = SlateSurface,
                            tonalElevation = 0.dp
                        ) {
                            NexusDestination.bottomBarPrimary.forEach { dest ->
                                val selected = currentDest == dest
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTo(dest) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.shortLabel
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = dest.shortLabel,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CyanAccent,
                                        selectedTextColor = CyanAccent,
                                        indicatorColor = CyanContainer,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_${dest.route}")
                                )
                            }
                            val isSecondaryDest = currentDest !in NexusDestination.bottomBarPrimary
                            NavigationBarItem(
                                selected = isSecondaryDest,
                                onClick = { scope.launch { drawerState.open() } },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Widgets,
                                        contentDescription = "All studio modules"
                                    )
                                },
                                label = { Text("Studio") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyanAccent,
                                    selectedTextColor = CyanAccent,
                                    indicatorColor = CyanContainer,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_studio_hub")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NexusNavHost(
                            navController = navController,
                            userDisplayName = userDisplayName,
                            userEmail = userEmail,
                            preferences = preferences,
                            workspaceViewModel = workspaceViewModel,
                            aiViewModel = aiViewModel,
                            settingsViewModel = settingsViewModel,
                            projectsState = projectsState,
                            briefsState = briefsState,
                            scriptsState = scriptsState,
                            calendarState = calendarState,
                            clipAnalysesState = clipAnalysesState,
                            researchAiState = researchAiState,
                            scriptAiState = scriptAiState,
                            shortsAiState = shortsAiState,
                            storyboardAiState = storyboardAiState,
                            clipIntelAiState = clipIntelAiState,
                            prefilledTopic = prefilledTopic,
                            prefilledAngle = prefilledAngle,
                            onNavigate = navigateTo,
                            onSignOut = { signOutUser(credentialManager, scope) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioDrawerContent(
    currentDest: NexusDestination,
    userDisplayName: String,
    onSelect: (NexusDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = CardSurface,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NEXUS AI",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyanAccent
                    )
                    StagePill(text = "STUDIO OS", accent = CyanAccent)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = userDisplayName.ifBlank { "Creator Workspace" },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        NavGroup.entries.forEach { group ->
            Text(
                text = group.title,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
            NexusDestination.entries.filter { it.group == group }.forEach { dest ->
                val selected = currentDest == dest
                NavigationDrawerItem(
                    selected = selected,
                    onClick = { onSelect(dest) },
                    icon = {
                        Icon(
                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                            contentDescription = dest.title,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = dest.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = CyanContainer,
                        selectedIconColor = CyanAccent,
                        selectedTextColor = MaterialTheme.colorScheme.onBackground,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .testTag("drawer_${dest.route}")
                )
            }
            HorizontalDivider(
                color = BorderSubtle,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun NexusNavHost(
    navController: NavHostController,
    userDisplayName: String,
    userEmail: String,
    preferences: StudioPreferences,
    workspaceViewModel: NexusWorkspaceViewModel,
    aiViewModel: NexusAiViewModel,
    settingsViewModel: SettingsViewModel,
    projectsState: com.example.data.model.UiState<List<com.example.data.model.CreatorProject>>,
    briefsState: com.example.data.model.UiState<List<com.example.data.model.ResearchBrief>>,
    scriptsState: com.example.data.model.UiState<List<com.example.data.model.GeneratedScript>>,
    calendarState: com.example.data.model.UiState<List<com.example.data.model.CalendarEntry>>,
    clipAnalysesState: com.example.data.model.UiState<List<com.example.data.model.ClipAnalysis>>,
    researchAiState: com.example.data.model.UiState<com.example.data.model.ResearchGenerationOutput>,
    scriptAiState: com.example.data.model.UiState<com.example.data.model.ScriptGenerationOutput>,
    shortsAiState: com.example.data.model.UiState<com.example.data.model.ShortsProductionPlan>,
    storyboardAiState: com.example.data.model.UiState<com.example.data.model.VideoStoryboardPlan>,
    clipIntelAiState: com.example.data.model.UiState<com.example.data.model.ClipIntelligenceOutput>,
    prefilledTopic: String,
    prefilledAngle: String,
    onNavigate: (NexusDestination) -> Unit,
    onSignOut: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = NexusDestination.DASHBOARD.route
    ) {
        composable(NexusDestination.DASHBOARD.route) {
            DashboardScreen(
                userName = userDisplayName,
                projectsState = projectsState,
                briefsState = briefsState,
                scriptsState = scriptsState,
                calendarState = calendarState,
                clipState = clipAnalysesState,
                onNavigate = onNavigate
            )
        }
        composable(NexusDestination.RESEARCH.route) {
            ResearchScreen(
                defaultPlatform = preferences.defaultPlatform,
                researchState = researchAiState,
                savedBriefsState = briefsState,
                onRunResearch = aiViewModel::runResearch,
                onSaveBrief = { out ->
                    workspaceViewModel.saveResearchBrief(
                        topic = out.topic,
                        audience = out.audience,
                        summary = out.summary,
                        viralScore = out.viralScore,
                        angles = out.angles,
                        keywords = out.keywords
                    )
                },
                onDeleteBrief = workspaceViewModel::deleteResearchBrief,
                onSendAngleToScript = { topic, angle ->
                    aiViewModel.primeScriptFromResearch(topic, angle)
                    onNavigate(NexusDestination.SCRIPT_GEN)
                }
            )
        }
        composable(NexusDestination.SCRIPT_GEN.route) {
            ScriptGenScreen(
                defaultTone = preferences.defaultTone,
                prefilledTopic = prefilledTopic,
                prefilledAngle = prefilledAngle,
                onConsumePrefill = aiViewModel::clearScriptPrefill,
                scriptState = scriptAiState,
                savedScriptsState = scriptsState,
                onGenerateScript = aiViewModel::generateScript,
                onSaveScript = { out, fmt, tone, dur ->
                    workspaceViewModel.saveScript(
                        title = out.title,
                        format = fmt,
                        tone = tone,
                        targetDurationSec = dur,
                        hook = out.hook,
                        body = out.body,
                        cta = out.cta,
                        visualCues = out.visualCues
                    )
                },
                onDeleteScript = workspaceViewModel::deleteScript
            )
        }
        composable(NexusDestination.SHORTS.route) {
            ShortsScreen(
                defaultShortDurationSec = preferences.defaultShortDurationSec,
                shortsState = shortsAiState,
                onGenerateShort = aiViewModel::generateShortsProduction,
                onSaveShortPackage = { plan ->
                    val bodyText = plan.beats.joinToString("\n\n") { beat ->
                        "[${beat.timecode}] (${beat.captionOverlay}) ${beat.spokenLine}\nVisual: ${beat.visualDirection}"
                    }
                    val cues = plan.beats.map { "${it.timecode}: ${it.visualDirection}" }
                    workspaceViewModel.saveScript(
                        title = plan.title,
                        format = ScriptFormat.SHORT,
                        tone = "Vertical Short",
                        targetDurationSec = plan.durationSeconds,
                        hook = plan.hooks.firstOrNull().orEmpty(),
                        body = bodyText,
                        cta = plan.hashtags.joinToString(" "),
                        visualCues = cues
                    )
                    workspaceViewModel.createProject(
                        title = plan.title,
                        niche = "Shorts",
                        format = ProjectFormat.SHORT,
                        stage = ProjectStage.SCRIPTED,
                        targetPlatform = "YouTube Shorts",
                        hook = plan.hooks.firstOrNull().orEmpty(),
                        notes = "Audio: ${plan.audioCue}"
                    )
                }
            )
        }
        composable(NexusDestination.VIDEO_STUDIO.route) {
            VideoStudioScreen(
                storyboardState = storyboardAiState,
                onGenerateStoryboard = aiViewModel::generateVideoStoryboard,
                onSaveStoryboardAsProject = { plan ->
                    val notes = buildString {
                        appendLine(plan.logline)
                        appendLine("Style: ${plan.visualStyleGuide}")
                        plan.scenes.forEach { s ->
                            appendLine("Scene ${s.sceneNumber} (${s.durationLabel}, ${s.shotType}): ${s.voiceover}")
                        }
                    }
                    workspaceViewModel.createProject(
                        title = plan.title,
                        niche = "Video Studio",
                        format = if (plan.aspectRatio == "9:16") ProjectFormat.SHORT else ProjectFormat.LONG_FORM,
                        stage = ProjectStage.IN_PRODUCTION,
                        targetPlatform = if (plan.aspectRatio == "9:16") "YouTube Shorts" else "YouTube",
                        hook = plan.logline,
                        notes = notes
                    )
                }
            )
        }
        composable(NexusDestination.CLIP_INTEL.route) {
            ClipIntelScreen(
                clipIntelState = clipIntelAiState,
                savedAnalysesState = clipAnalysesState,
                onAnalyzeClip = aiViewModel::analyzeClip,
                onSaveAnalysis = { out, rawNotes ->
                    workspaceViewModel.saveClipAnalysis(
                        clipTitle = out.clipTitle,
                        sourceSummary = rawNotes,
                        retentionScore = out.retentionScore,
                        hookStrength = out.hookStrength,
                        pacingVerdict = out.pacingVerdict,
                        highlights = out.highlights,
                        improvements = out.improvements
                    )
                },
                onDeleteAnalysis = workspaceViewModel::deleteClipAnalysis
            )
        }
        composable(NexusDestination.PROJECTS.route) {
            ProjectsScreen(
                defaultPlatform = preferences.defaultPlatform,
                projectsState = projectsState,
                onCreateProject = { title, niche, fmt, stage, plat, hook, notes ->
                    workspaceViewModel.createProject(
                        title = title,
                        niche = niche,
                        format = fmt,
                        stage = stage,
                        targetPlatform = plat,
                        hook = hook,
                        notes = notes
                    )
                },
                onAdvanceStage = workspaceViewModel::advanceProjectStage,
                onDeleteProject = workspaceViewModel::deleteProject
            )
        }
        composable(NexusDestination.CALENDAR.route) {
            CalendarScreen(
                defaultPlatform = preferences.defaultPlatform,
                calendarState = calendarState,
                onCreateEntry = { title, plat, date, time, status, notes ->
                    workspaceViewModel.createCalendarEntry(
                        title = title,
                        platform = plat,
                        scheduledDate = date,
                        scheduledTime = time,
                        status = status,
                        notes = notes
                    )
                },
                onCycleStatus = workspaceViewModel::cycleCalendarStatus,
                onDeleteEntry = workspaceViewModel::deleteCalendarEntry
            )
        }
        composable(NexusDestination.ANALYTICS.route) {
            AnalyticsScreen(
                projectsState = projectsState,
                briefsState = briefsState,
                scriptsState = scriptsState,
                calendarState = calendarState,
                clipState = clipAnalysesState
            )
        }
        composable(NexusDestination.SETTINGS.route) {
            SettingsScreen(
                userDisplayName = userDisplayName,
                userEmail = userEmail,
                preferences = preferences,
                onPlatformChange = settingsViewModel::setDefaultPlatform,
                onToneChange = settingsViewModel::setDefaultTone,
                onShortDurationChange = settingsViewModel::setDefaultShortDuration,
                onCompactDensityChange = settingsViewModel::setCompactDensity,
                onDynamicColorChange = settingsViewModel::setDynamicColor,
                onSignOut = onSignOut
            )
        }
    }
}
