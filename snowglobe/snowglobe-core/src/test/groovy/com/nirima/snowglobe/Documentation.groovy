package com.nirima.snowglobe

import com.nirima.snowglobe.core.DataSource
import com.nirima.snowglobe.core.Provider
import com.nirima.snowglobe.core.Resource
import com.nirima.snowglobe.core.SGItem
import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager
import org.codehaus.groovy.tools.groovydoc.FileOutputTool
import org.codehaus.groovy.tools.groovydoc.FileSystemResourceManager
import org.codehaus.groovy.tools.groovydoc.GroovyDocTool
import org.codehaus.groovy.tools.groovydoc.LinkArgument
import org.codehaus.groovy.tools.groovydoc.gstringTemplates.GroovyDocTemplateInfo
import org.reflections.Reflections

import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.nio.file.Path

class Documentation {
    private static final String TEMPLATES_DIR = "org/codehaus/groovy/tools/groovydoc/gstringTemplates";

    private String[] sourcePath;
    private File destDir;
    private List<String> packageNames = new ArrayList();
    private List<String> excludePackageNames = new ArrayList();
    private String windowTitle = "Groovy Documentation";
    private String docTitle = "Groovy Documentation";
    private String footer = "Groovy Documentation";
    private String header = "Groovy Documentation";
    private Boolean privateScope = false;
    private Boolean protectedScope = false;
    private Boolean packageScope = false;
    private Boolean publicScope = false;
    private Boolean author = true;
    private Boolean processScripts = true;
    private Boolean includeMainForScripts = true;
    private boolean useDefaultExcludes = true;
    private boolean includeNoSourcePackages = false;
    private Boolean noTimestamp = false;
    private Boolean noVersionStamp = false;
    //private List<DirSet> packageSets = new ArrayList();
    private List<String> sourceFilesToDoc = new ArrayList();
    private List<LinkArgument> links = new ArrayList();
    private File overviewFile;
    private File styleSheetFile;
    private String extensions = ".java:.groovy:.gv:.gvy:.gsh";
    private String charset;
    private String fileEncoding;

    File fileRoot;

    public static void main(String[] args) {

        Documentation doc = new Documentation();
        doc.go();
    }


    public void go() {
        List<String> packagesToDoc = new ArrayList();

        this.sourcePath =  [ "/Users/magnayn/dev/nirima/snowglobe/snowglobe/snowglobe-core/src/main/groovy/com/nirima/snowglobe/consul",
                             "/Users/magnayn/dev/nirima/snowglobe/snowglobe/snowglobe-core/src/main/groovy/com/nirima/snowglobe/docker"];

        Path sourceDirs = new File("/Users/magnayn/dev/nirima/snowglobe/snowglobe/snowglobe-core/src/main/groovy/com/nirima/snowglobe/docker").toPath();

        Properties properties = new Properties();
        properties.setProperty("windowTitle", this.windowTitle);
        properties.setProperty("docTitle", this.docTitle);
        properties.setProperty("footer", this.footer);
        properties.setProperty("header", this.header);

        properties.setProperty("publicScope", this.publicScope.toString());
        properties.setProperty("protectedScope", this.protectedScope.toString());
        properties.setProperty("packageScope", this.packageScope.toString());
        properties.setProperty("privateScope", this.privateScope.toString());
        properties.setProperty("author", this.author.toString());
        properties.setProperty("processScripts", this.processScripts.toString());
        properties.setProperty("includeMainForScripts", this.includeMainForScripts.toString());
        properties.setProperty("overviewFile", this.overviewFile != null ? this.overviewFile.getAbsolutePath() : "");
        properties.setProperty("charset", this.charset != null ? this.charset : "");
        properties.setProperty("fileEncoding", this.fileEncoding != null ? this.fileEncoding : "");
        properties.setProperty("timestamp", Boolean.valueOf(!this.noTimestamp).toString());
        properties.setProperty("versionStamp", Boolean.valueOf(!this.noVersionStamp).toString());

        destDir = new File("/tmp/dox");

        fileRoot = new File("/Users/magnayn/dev/nirima/snowglobe/docs/reference/docker");
        sourceFilesToDoc.add("Docker.groovy");
        process(properties)

        sourceFilesToDoc.clear();
        fileRoot = new File("/Users/magnayn/dev/nirima/snowglobe/docs/reference/consul");
        sourceFilesToDoc.add("Consul.groovy");
        process(properties)



        // Providers



//        Set<Class<? extends DataSource>> dsTypes = reflections.getSubTypesOf(DataSource.class);
//        documentClasses(dsTypes)

    }

    private void process(Properties properties) {
        GroovyDocTool htmlTool = new GroovyDocTool(new ClasspathResourceManager(),
                                                   this.sourcePath,
                                                   this.getDocTemplates(),
                                                   this.getPackageTemplates(),
                                                   this.getClassTemplates(),
                                                   this.links,
                                                   properties);





        htmlTool.add(this.sourceFilesToDoc);
        FileOutputTool output = new FileOutputTool();
        htmlTool.renderToOutput(output, "/tmp/dox");

        // At this point the /tmp/dox directory has
        Reflections reflections = new Reflections("com.nirima");

        Set<Class<? extends Provider>> prypes = reflections.getSubTypesOf(Provider.class);
        prypes.each() { it -> documentSimple(it, 1) };

        Set<Class<? extends Resource>> subTypes = reflections.getSubTypesOf(Resource.class);

        documentClasses(subTypes)
    }
    private void documentSimple(
            Class<?> it, level) {
        SGItem name = it.getAnnotation(SGItem.class);


        File f;

        if( name == null )
            f = new File(fileRoot, it.simpleName + ".md");
        else
            f = new File(fileRoot, name.value() + ".md");

        
        documentSimple(it, level, f);
    }

    private void documentSimple(
           Class<?> it, level, File f) {

        if(Modifier.isAbstract(it.getModifiers()))
            return;

         

            SGItem name = it.getAnnotation(SGItem.class);

        (level-1).times { f << "  " };
        
        level.times { f << "#" };
            if( name != null ) {

                f << " ${name.value()}\n";
            }  else {
                f << " ${it.simpleName}\n"
            }

           // println " ::${stateType}";


            JavadocXmlWrapper wrapper = getWrapperFor(it);
        (level-1).times { f << "  " };
            f << wrapper.description + "\n";

            it.fields.each() { p ->
                if( p.name != '__$stMC' && p.name != 'closure') {
                    (level-1).times { f << "  " };
                    f << "- ${p.name} (${p.type.simpleName})\n\n";
                    (level).times { f << "  " };
                    f << wrapper.getFieldDescription(p.name) + "\n\n";
                }
            }


    }

    private void documentClasses(
            Set<Class<?>> subTypes) {
        subTypes.each() { it ->


            Class<?> stateType = ((ParameterizedType) it.getGenericSuperclass())
                    .getActualTypeArguments()[0];

            SGItem name = it.getAnnotation(SGItem.class);


            File f = new File(fileRoot, name.value() + ".md");
            f.delete();

            f << "---------------------\n";
            f << "# ${name.value()}\n";


            // println " ::${stateType}";


            JavadocXmlWrapper wrapper = getWrapperFor(it);
            JavadocXmlWrapper statewrapper = getWrapperFor(stateType);

            f << wrapper.description + "\n";

            stateType.fields.each() { p ->
                if( p.name != '__$stMC' && p.name != 'closure') {
                    f << "- ${p.name} (${p.type.simpleName})\n\n";
                    f << statewrapper.getFieldDescription(p.name) + "\n\n";


                    if( p.genericType instanceof ParameterizedType ) {
                        Class<?> itemType = p.genericType
                                .getActualTypeArguments()[0];

                        if( itemType != String.class && itemType != Object.class )
                            documentSimple(itemType,2, f);


                    }

                }
            }

        }
    }


    protected String[] getPackageTemplates() {
        return [ TEMPLATES_DIR + "/packageLevel/packageDocStructuredData.xml"];
    }

    protected String[] getDocTemplates() {
        return  [ TEMPLATES_DIR + "/topLevel/rootDocStructuredData.xml"];
    }


    protected String[] getClassTemplates() {
        return  [ "com/nirima/snowglobe/doc/classDocStructuredData.xml"];
    }

    JavadocXmlWrapper getWrapperFor(Class stateType) {
        try {
        File f = new File("/tmp/dox/DefaultPackage/${stateType.simpleName}.html");
        return new JavadocXmlWrapper(f);      }
            catch(Exception ex) {
                return new JavadocXmlWrapper();
            }
        
    }

    public static final String[] DOC_TEMPLATES = ["org/codehaus/groovy/tools/groovydoc/gstringTemplates/topLevel/index.html", "org/codehaus/groovy/tools/groovydoc/gstringTemplates/topLevel/overview-frame.html", "org/codehaus/groovy/tools/groovydoc/gstringTemplates/topLevel/allclasses-frame.html", "org/codehaus/groovy/tools/groovydoc/gstringTemplates/topLevel/overview-summary.html", "org/codehaus/groovy/tools/groovydoc/gstringTemplates/topLevel/help-doc.html", "org/codehaus/groovy/tools/groovydoc/gstringTemplates/topLevel/index-all.html", "org/codehaus/groovy/tools/groovydoc/gstringTemplates/topLevel/deprecated-list.html", "org/codehaus/groovy/tools/groovydoc/gstringTemplates/topLevel/stylesheet.css",
                                                  "org/codehaus/groovy/tools/groovydoc/gstringTemplates/topLevel/inherit.gif", "org/codehaus/groovy/tools/groovy.ico"];

    public static final String[] CLASS_TEMPLATES = [
            //"org/codehaus/groovy/tools/groovydoc/gstringTemplates/classLevel/classDocName.html",
                                               //     "org/codehaus/groovy/tools/groovydoc/gstringTemplates/classLevel/classDocStructuredData.xml"
          "com/nirima/snowglobe/doc/classDocStructuredData.xml"];

}

class JavadocXmlWrapper
{
    def data;

    JavadocXmlWrapper() {
        data = new XmlSlurper(false,false).parseText("<class></class>");
    }

    JavadocXmlWrapper(File f) {
        data = new XmlSlurper(false,false).parseText(f.text);
    }

    String getDescription() {
        return data.comment;
    }

    String getFieldDescription(String name) {
        try {
            def fx = data.fields;
            def fy = data.fields.field;
            return data.fields.field.find( {it.@name == name}).comment;
        }   catch(Exception ex) {

        }
        return "";
    }
}
