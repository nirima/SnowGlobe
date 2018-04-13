/*
  ()                       () || |      | |
  /\  _  _    __           /\/|| |  __  | |   _
 /  \/ |/ |  /  \_|  |  |_/   ||/  /  \_|/ \_|/
/(__/  |  |_/\__/  \/ \/ /(__/ |__/\__/  \_/ |__/
*/


import groovy.transform.Field

@Field
def buildEnv;



//////////////////////////////////////////////////////



node {
   
  stage(name:"Checkout") {
    checkout scm;
     buildEnv = load 'jenkins/buildEnv.groovy';
     buildEnv(currentBuild);
  }

  stage(name:"Build") {
    build()
  }

  stage(name:"Publish") {
    publishDocker()
  }

  stage(name:"Deploy") {

    if( buildEnv.isPR() ) {
        // Pull requests : Build a test environment
        def testurl = spinUpTestEnvironment();
        slackSend channel: "#snowglobe", message: "Docker :whale: test environment :snowflake: available now  - ${env.JOB_NAME} ${env.BUILD_NUMBER} - ${testurl}"

        commentOnPR(buildEnv.PR(), "Test environment :snowflake: available now : ${testurl}" )
      } else {
          // Master builds : Continuously Deploy
          updateDeployedVersion();
          slackSend channel: "#snowglobe", message: "Docker :whale: LIVE ENV UPDATED - ${env.JOB_NAME} ${env.BUILD_NUMBER} - ${testurl}"
      }
  }
}


def build() {
  
  def mvnHome = tool 'latest'
  env.MAVEN_OPTS="-Xmx2G";
  withJavaEnv {
    sh "${mvnHome}/bin/mvn -DskipTests clean install"
  }
}

def publishDocker() {

      // Prepare
      sh './prod/build.sh';



        if( buildEnv.isPR() ) {
            String tags = buildEnv.buildTags("dev.nirima.com", "snowglobe");
            def image = docker.build(tags, "-f prod/Dockerfile prod");

          withDockerRegistry([ credentialsId: "registry_nirima", url: "" ]) {
            image.push();
            image.push('latest');
          }
        }
        else {
            String tags = "nirima/snowglobe:${env.BUILD_NUMBER}";
             def image = docker.build(tags, "-f prod/Dockerfile prod");

              withDockerRegistry([ credentialsId: "docker_hub", url: "" ]) {
                image.push();
                image.push('latest');
              }
        }
      

}

def updateDeployedVersion() {
    // Stage 1: Just re-apply a snowglobe
   snowglobe_apply globeId: 'snowglobe-cd'
}

def spinUpTestEnvironment() {

      String name1 = "${buildEnv.branch}".replace("/","-").toLowerCase();

      String properties = """name="${name1}"
      build=${env.BUILD_NUMBER}""";

      String name = "${name1}-${env.BUILD_NUMBER}";

      snowglobe_clone createAction: true, sourceId: 'snowglobe-ci-template', targetId: name

      snowglobe_set_variables globeId: name, variables: properties

      snowglobe_apply globeId: name

      def response = snowglobe_state globeId: name


      //def info_response = readJSON text: response.content;
      def info_response = readJSON text: response;

      def ip = info_response['modules']['base']['resources']['docker_container_info']['realtime']['items']['info']['NetworkSettings']['IPAddress'];

      return "http://${ip}:8888/";
}

void withJavaEnv(List envVars = [], def body) {
    String npmTool = tool name: 'Node', type: 'jenkins.plugins.nodejs.tools.NodeJSInstallation'
   echo npmTool;

   List javaEnv = ["PATH+JDK=${npmTool}/bin"]

    // Add any additional environment variables.
    javaEnv.addAll(envVars)

    // Invoke the body closure we're passed within the environment we've created.
    withEnv(javaEnv) {
        body.call()
    }
}