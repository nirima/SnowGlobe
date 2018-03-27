node {
  stage name:"Checkout"
  checkout scm;

  stage name:"Build"
  build()

  stage name:"Publish"
  publishDocker()
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

      //String tags = "nirima/snowglobe:${env.BUILD_NUMBER}";
      String tags = "nirima/snowglobe:latest";

      def image = docker.build(tags, "-f prod/Dockerfile prod");

      withDockerRegistry([ credentialsId: "docker_hub", url: "" ]) {
        image.push();
      }
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