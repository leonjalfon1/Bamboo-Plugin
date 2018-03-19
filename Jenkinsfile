def server
def buildInfo
def String fileName = ""


def revertSnapshot() {
    bat 'c:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe -noe -c ". \\"C:\\Program Files (x86)\\VMware\\Infrastructure\\PowerCLI\\Scripts\\Initialize-PowerCLIEnvironment.ps1\\" $true"; c:\\scripts\\VMware_Revert.ps1 -vm ' + machine_name + ' -SnapshotName ' + SnapshotName + ' -VMHostName "VcenterServer01.dm.cx"  -VMServerUsername "dm\\tfs" -VMServerPassword "Tfs12345"'
}

def powerOnServer() {
    bat 'C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe -noe -c ". \\"C:\\Program Files (x86)\\VMware\\Infrastructure\\PowerCLI\\Scripts\\Initialize-PowerCLIEnvironment.ps1\\" $true"; c:\\scripts\\VMware_PowerOn.PS1 -vm ' + machine_name + ' -VMHostName "VcenterServer01.dm.cx"  -VMServerUsername "dm\\tfs" -VMServerPassword "Tfs12345"'
}


def loadFile(filename) {
    fileName = readFile encoding: 'unicode', file: 'c:\\installer\\newName.txt'
    fileName = fileName.replaceAll("\\p{C}", "");
}


pipeline {
    agent {
        node{label 'ci-plugins-2016'}
    }
	
	
    stages {

        stage('Pipeline Info') {
            steps {
                echo bat(returnStdout: true, script: 'set')
            }
        }

        stage('Clone Source') {
            steps {
            
				git branch: Branch, credentialsId: 'e0d53845-d2e3-42f6-8c52-39c2e31d2bac', url: 'http://tfs2015app:8080/tfs/DefaultCollection/External%20Enterprise/_git/Bamboo-Plugin'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -f ./pom.xml clean install -Dmaven.test.skip=true'
            }
        }

        stage('Pack') {
            steps {
                archiveArtifacts artifacts: 'Installer\\target\\media\\Risk_Management_windows-x64_8_60_0.exe', onlyIfSuccessful: true
            }
        }

        stage('Archive Artifact') {
            steps {
                script {
                    writeFile file: "buildNumber.txt", text: "${env.BUILD_NUMBER}"
                }

                powershell ''' If(Test-Path "ARM-Root\\pom.xml")	{
                        Write-Host "pom.xml exists"
                   		[xml]$XmlDocument = Get-Content -Path "ARM-Root\\pom.xml"
                        $version = $XmlDocument.project.version
                        Write-Host "Version found $version"
                        $buildNumber = Get-Content buildNumber.txt
                        $archiveName = "Risk_Management_windows-x64" + "-$version" + "." + "$buildNumber" + ".zip"
						$archiveName | Out-File c:\\installer\\newName.txt

                        if (-not (test-path "$env:ProgramFiles\\7-Zip\\7z.exe")) {throw "$env:ProgramFiles\\7-Zip\\7z.exe needed"}
                        set-alias Compress "$env:ProgramFiles\\7-Zip\\7z.exe"
                        $Source1="Installer\\target\\media\\Risk_Management*.exe"
                        $Source2="ARM-Root\\artifacts"
						$Source3="ARM-DbScripts"
                        $Target="Output\\$archiveName"
                        Compress a -r $Target $Source1 $Source2 $Source3

				}'''
                loadFile(fileName)
                bat "echo *******************"
                bat """echo $fileName"""
            }
        }

        stage('Upload to Artifactory') {
            steps {
                script {
                    server = Artifactory.server "-484709638@1439224648400"
                    buildInfo = Artifactory.newBuildInfo()
                    buildInfo.env.capture = true
                    buildInfo.env.collect()
                    def uploadSpec = ""
                    uploadSpec = """{
                        "files": [
                        {
                        "pattern": "Output/*.zip",
                        "target": "CxARM/"
                        }
                        ]
						}"""

                    server.upload spec: uploadSpec, buildInfo: buildInfo
                }
            }
            post {
                success {
                    cleanWs()
                    println("upload to artifactory was completed successfully")
                }
				failure {
					cleanWs()
					println("upload to artifactory Failed")
				}
			}
        }

            
         


    }

}