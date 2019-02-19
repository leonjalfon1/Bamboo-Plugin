pipeline {
  
  parameters {        
        booleanParam(name: 'IsReleaseBuild', description: 'Check the box if you want to create a release build') 
    }
  
  agent {
    node {
      label 'Plugins'
    }

  }
  stages {
    stage('Remove Snapshot') {
      steps {
        
        powershell '''#------------------------------------------------------------------------------------------------------------
# REMOVE THE WORD SNAPSHOT (ONLY FOR RELEASE BUILDS)
#------------------------------------------------------------------------------------------------------------

[string]$IsReleaseBuild = $ENV:IsReleaseBuild
[string]$RootPath = "C:\\CI-Slave\\workspace\\$ENV:JOB_NAME"


If($IsReleaseBuild -eq "true")
{
    Write-Host " ----------------------------------------------------- "
    Write-Host "|  SNAPSHOT DISABLED: Removing Snapshot before build  |"
    Write-Host " ----------------------------------------------------- "

    $XmlPath = $RootPath + "\\pom.xml"

    If(Test-Path "$XmlPath")
    {  
        [xml]$XmlDocument = Get-Content -Path $XmlPath
        $XmlDocument.project.version = $XmlDocument.project.version.Replace("-SNAPSHOT", "")
        $XmlDocument.Save($XmlPath)
    }
}
Else
{
    Write-Host " ----------------------------------------------------- "
    Write-Host "|    SNAPSHOT ENABLED: Run Build without modifying    |"
    Write-Host " ----------------------------------------------------- " 
}'''

      }
    }
	
    stage('Build') {
      steps {
        bat """mvn clean install -Dorg.apache.maven.user-settings=C:\\Jenkins\\workspace\\settings.xml -Dbuild.number=${BUILD_NUMBER}"""
      }
    }
	  
    stage('Archive Artifacts') {
      steps {
        archiveArtifacts 'target/*.jar'
      }
    }
	  
  }
}
