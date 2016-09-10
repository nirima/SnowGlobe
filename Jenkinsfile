node {
  stage name:"Checkout"
  checkout scm;

  // No longer required
  //stage name:"Dependencies"
  //buildTempDeps()

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

      String tags = "dev.nirima.com/snowglobe:${env.BUILD_NUMBER}";

      def image = docker.build(tags, "-f prod/Dockerfile prod");

      image.push();
}


def buildTempDeps() {
	// Temporary dependencies to things that have not yet been fixed
	dir('docker-java') {
        
		git url: "https://github.com/magnayn/docker-java.git"

		def mvnHome = tool 'latest'
		env.MAVEN_OPTS="-Xmx2G";

		sh "${mvnHome}/bin/mvn -DskipTests clean install"
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