node {
  stage name:"Checkout"
  checkout scm;

  stage name:"Dependencies"
  buildTempDeps() 

  stage name:"Build"
  build()

}


def build() {
  
  def mvnHome = tool 'latest'
  env.MAVEN_OPTS="-Xmx2G";

  sh "${mvnHome}/bin/mvn clean install"
  
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