package org.apache.maven.cli;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MavenGroovyConfig
{
    private static final String MVN_MAVEN_GROOVY_CONFIG = ".mvn/maven.groovy";
    private final CLIManager cliManager;
    private final CommandLine cli;

    public MavenGroovyConfig( CLIManager cliManager, CommandLine cli )
    {
        this.cliManager = cliManager;
        this.cli = cli;
    }

    public CommandLine read( File projectDirectory ) throws Exception
    {
        try
        {
            File scriptFile = new File( projectDirectory, MVN_MAVEN_GROOVY_CONFIG );
            if ( !scriptFile.isFile() )
            {
                return null;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("cli", cli);

            List<String> args = new ArrayList<>();
            params.put("args", args);
            Binding scriptEnv = new Binding(params);

            GroovyShell shell = new GroovyShell();
            Script script = shell.parse(scriptFile);
            script.setBinding(scriptEnv);
            script.run();

            CommandLine mavenConfig = cliManager.parse( args.toArray( new String[0])  );
            boolean containsOnlyOptions = mavenConfig.getArgList().isEmpty();
            if ( !containsOnlyOptions )
            {
                throw new ParseException( "Unrecognized maven.groovy entries: " + mavenConfig.getArgList() );
            }

            return mavenConfig;
        }
        catch ( Exception e )
        {
            System.err.println( "Unable to parse maven.groovy: " + e.getMessage() );
            cliManager.displayHelp( System.out );
            throw e;
        }
    }
}
