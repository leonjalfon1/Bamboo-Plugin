pipeline {

    agent {
        node { label 'Plugins' }
    }

    tools {
        maven 'mvn_3.3.3_windows'
        jdk 'JDK_WINDOWS_1.8.0_92'
    }

    parameters
            {
                booleanParam(defaultValue: false, description: 'Check the box if you want to create a release build', name: 'IsReleaseBuild')
				string(name: 'BranchName', defaultValue: 'master', description: 'Branch used by the job')
            }

    stages {

        stage('Pipeline Info') {
            steps {
                echo bat(returnStdout: true, script: 'set')
            }
        }

        stage('Remove Snapshot from build') {
            when {
                expression {
                    return params.IsReleaseBuild
                }
            }
            steps {
                echo " ----------------------------------------------------- "
                echo "|  SNAPSHOT DISABLED: Removing Snapshot Before Build  |"
                echo " ----------------------------------------------------- "

                script {
                    workspacePath = pwd()
                }

                dir("$workspacePath") {
                    powershell '''		If(Test-Path pom.xml)
					{  
						[xml]$XmlDocument = Get-Content -Path pom.xml
						$XmlDocument.project.version = $XmlDocument.project.version.Replace("-SNAPSHOT", "")
						$XmlDocument.Save("$pwd\\pom.xml")
					}'''
                }

                dir("$workspacePath\\build") {
                    powershell '''		If(Test-Path pom.xml)
					{  
						[xml]$XmlDocument = Get-Content -Path pom.xml
						$XmlDocument.project.version = $XmlDocument.project.version.Replace("-SNAPSHOT", "")
						$XmlDocument.Save("$pwd\\pom.xml")
					}'''
                }

                dir("$workspacePath\\cxplugin-agent") {
                    powershell '''		If(Test-Path pom.xml)
					{  
						[xml]$XmlDocument = Get-Content -Path pom.xml
						$XmlDocument.project.version = $XmlDocument.project.version.Replace("-SNAPSHOT", "")
						$XmlDocument.Save("$pwd\\pom.xml")
					}'''
                }

                dir("$workspacePath\\cxplugin-common") {
                    powershell '''		If(Test-Path pom.xml)
					{  
						[xml]$XmlDocument = Get-Content -Path pom.xml
						$XmlDocument.project.version = $XmlDocument.project.version.Replace("-SNAPSHOT", "")
						$XmlDocument.Save("$pwd\\pom.xml")
					}'''
                }

                dir("$workspacePath\\cxplugin-server") {
                    powershell '''		If(Test-Path pom.xml)
					{  
						[xml]$XmlDocument = Get-Content -Path pom.xml
						$XmlDocument.project.version = $XmlDocument.project.version.Replace("-SNAPSHOT", "")
						$XmlDocument.Save("$pwd\\pom.xml")
					}'''
                }
            }
        }

        stage('Build') {
            steps {
                bat "mvn clean install -Dbuild.number=${BUILD_NUMBER}"
            }
        }

        stage('Apply Artifact Version') {
            steps {
                script {
                    workspacePath = pwd()
                    writeFile file: "$workspacePath\\buildNumber.txt", text: "${env.BUILD_NUMBER}"
                }

                dir("$workspacePath") {

                    powershell ''' If(Test-Path pom.xml)
					{ 
						[xml]$XmlDocument = Get-Content -Path pom.xml
                        $version = $XmlDocument.project.version.Split(\'-\')[0]
                        $fileName = Get-ChildItem target\\*.zip | Split-Path -Leaf
                        $buildNumber = Get-Content buildNumber.txt
                        $newName = $fileName.Split('.')[0] + "-$version" + "." + "$buildNumber" + ".zip"
                        $newName | Out-File c:\\newName.txt
                        Get-ChildItem target\\*.zip | Rename-Item -NewName {$newName}
					}'''
                }

                loadFile(fileName)
                bat """echo $fileName"""
            }
        }
/*
        stage('Upload To Artifactory') {
            steps {
                script {
                    server = Artifactory.server "-484709638@1439224648400"
                    buildInfo = Artifactory.newBuildInfo()
                    buildInfo.env.capture = true
                    buildInfo.env.collect()
                    def uploadSpec = ""

                    if ("${params.IsReleaseBuild}" == "true") {
                        uploadSpec = """{
                        "files": [
                        {
                        "pattern": "target/*.zip",
                        "target": "plugins-release-local/com/checkmarx/teamcity/"
                        }
                        ]
                        }"""
                    } else {
                        uploadSpec = """{
                        "files": [
                        {
                        "pattern": "target/*.zip",
                        "target": "plugins-snapshot-local/com/checkmarx/teamcity/"
                        }
                        ]
                        }"""
                    }
                    server.upload spec: uploadSpec, buildInfo: buildInfo
                }
            }

		}

*/

	}
}
