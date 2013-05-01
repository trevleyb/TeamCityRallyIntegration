TeamCityRallyIntegration
========================

This is a plugin for TeamCity which integrates TeamCity with Rally. 

Basically this creates BUILD records in Rally Associated with a BUILD DEFINITION record. BUILD DEFINITION records are created whenever you create a project in Rally and they have a default name of “DEFAULT BUILD”. You can have multiple BUILD DEFINITIONS associated with a PROJECT however there is no interface or way to create these in RALLY (Rally, you guys need to sort this). 

When you configure a TeamCIty build configuration to talk to RALLY and you specify a build definition that does not exist there is an option to have the integrator create it for you automatically. This feature is needed as there is no other way to create BUILD DEFINITIONS through the UI (you can edit thier names but canot create new ones). 

For a link with stories, work you will need to:

1.  Configure SubVersion or Git to integrate with Rally.
2.	When a source-check-in occurs the check-in details are added to Rally as a ChangeSet record (by the source intergator) which are associated with the relevant user story or defect. You should be able to go to a story, go to its details and see the associated check-ins. 
3.	This should then trigger a TeamCity build (right)
4.	When the build is finished the TeamCityRallyIntegrator is triggered. It will look at the check-ins associated with the build (as provided by TeamCity), find them in Rally, link them to the build record and create a build record. 

To configure an individual build configuration to work with a project in Rally you need to create Build properties in TeamCity which point at Rally. 

There are four properties you need to create:

RallyWorkspace, RallyProject, RallyBuildDef and RallySCM. 

Each of these are simply the TEXTUAL names of the workspace, project, build definition and SCMFRepository name as defined in Rally.  

If these three properties do not exist then nothing will be created in Rally. Finally, you can turn on TEST ONLY mode (in settings) and can monitor everything through the log file without it doing anything in Rally. Also you can turn on CreateNotExist in the config which will create BuildDefinitions if they do not already exist. 

