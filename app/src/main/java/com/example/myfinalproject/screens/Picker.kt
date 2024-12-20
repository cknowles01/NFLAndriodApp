package com.example.myfinalproject.screens


import android.annotation.SuppressLint
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DisplayMode.Companion.Picker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myfinalproject.R
import com.example.myfinalproject.order.Conference
import com.example.myfinalproject.order.Division
import com.example.myfinalproject.order.OrderViewModel
import com.example.myfinalproject.order.Team


enum class PickerScreen(val title: String) {
    Conference(title = "Pick conference"),
    Division(title = "Pick division"),
    Team(title = "Pick team"),
    TeamData(title = "Team Info")
}

@Composable
fun ConferencePicker(
    conferences: List<Conference>,
    onConferenceSelected: (Conference) -> Unit,
) {
    var selectedConference by remember { mutableStateOf<Conference?>(null) }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        LazyColumn {
            items(conferences) { conference ->
                if (conferences.isEmpty()) {
                    Log.d("ConferencePicker", "No conferences available")
                    Text("No conferences available", modifier = Modifier.padding(16.dp))
                }

                Button(
                    onClick = { onConferenceSelected(conference) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(text = conference.name)

                }
            }
        }
        Image(
            painter = painterResource(id = R.drawable.nfl_logo),
            contentDescription = null
        )
    }
}

@Composable
fun DivisionPicker(
    divisions: List<Division>,
    onDivisionSelected: (Division) -> Unit,
    selectedConference: String
) {

    var selectedDivision by remember { mutableStateOf<Division?>(null) }

    LazyColumn {
        items(divisions) { division ->
            Button(
                onClick = {
                    selectedDivision = division
                    onDivisionSelected(division)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = "${selectedConference} ${division.name}")
            }
        }
    }
}

@Composable
fun TeamPicker(teams: List<Team>, onTeamSelected: (Team) -> Unit) {
    var selectedTeam by remember { mutableStateOf<Team?>(null) }

    if (teams.isEmpty()) {
        Text("Teams in TeamPicker is empty")
    }
    LazyColumn {
        items(teams) { team ->
            Button(
                onClick = {
                    selectedTeam = team
                    onTeamSelected(team)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = team.Name)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFLApp(
    viewModel: OrderViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    navController: NavHostController = rememberNavController()
    ) {

    //makes sure this only runs once on launch
    LaunchedEffect(Unit) {
        viewModel.fetchTeams()
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = PickerScreen.valueOf(
        backStackEntry?.destination?.route ?: PickerScreen.Conference.name
    )
    Scaffold(
        topBar = {
            NFLAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() })
        }
    ) {
        innerPadding ->
        val uiState by viewModel.uiState.collectAsState()
        NavHost(
            navController = navController,
            startDestination = PickerScreen.Conference.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = PickerScreen.Conference.name) {
                ConferencePicker(
                    conferences = uiState.conferences,
                    onConferenceSelected = { conference ->
                        viewModel.onConferenceSelected(conference)
                        navController.navigate(PickerScreen.Division.name) },
                )
            }
            composable(route = PickerScreen.Division.name) {
                uiState.selectedConference?.let { selectedConference ->
                    val conferenceName = selectedConference.name
                    DivisionPicker(
                        divisions = uiState.selectedConference?.division ?: emptyList(),
                        onDivisionSelected = { division ->
                            viewModel.onDivisionSelected(division, conference = selectedConference)
                            navController.navigate(PickerScreen.Team.name)
                        },
                        selectedConference = conferenceName
                    )
                }
            }
            composable(route = PickerScreen.Team.name) {
                val teamsInDivision = uiState.teams
                TeamPicker(teams = teamsInDivision, onTeamSelected = {  team ->
                    uiState.selectedTeam = team
                    navController.navigate(PickerScreen.TeamData.name)

                }) 
            }
            composable(route = PickerScreen.TeamData.toString()) {
                uiState.selectedTeam?.let { it1 -> DisplayTeamDetails(team = it1) }
            }

        }
        

    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFLAppBar(
    currentScreen: PickerScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = { Text(currentScreen.title) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "goback"
                    )
                }
            }
        }
    )
}


