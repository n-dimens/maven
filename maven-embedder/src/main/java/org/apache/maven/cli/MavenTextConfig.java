package org.apache.maven.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MavenTextConfig
{
    private static final String MVN_MAVEN_CONFIG = ".mvn/maven.config";

    private final CLIManager cliManager;

    public MavenTextConfig(CLIManager cliManager )
    {
        this.cliManager = cliManager;
    }

    public CommandLine read( File projectDirectory ) throws Exception
    {
        try
        {
            File configFile = new File( projectDirectory, MVN_MAVEN_CONFIG );
            if ( configFile.isFile() )
            {
                String[] args = parseMavenConfig( configFile );
                CommandLine mavenConfig = cliManager.parse( args );
                boolean containsOnlyOptions = mavenConfig.getArgList().isEmpty();
                if ( !containsOnlyOptions )
                {
                    throw new ParseException( "Unrecognized maven.config entries: " + mavenConfig.getArgList() );
                }

                return mavenConfig;
            }

            return null;
        }
        catch ( ParseException e )
        {
            System.err.println( "Unable to parse maven.config: " + e.getMessage() );
            cliManager.displayHelp( System.out );
            throw e;
        }
    }

    private String[] parseMavenConfig( File config ) throws IOException
    {
        List<String> args = new ArrayList<>();
        String configContent = new String( Files.readAllBytes( config.toPath() ) );
        String[] lines = configContent.split( "\n" );
        for ( String line : lines )
        {
            if ( line.startsWith( "#" ) )
            {
                continue;
            }

            for ( String arg : line.split( "\\s+" ) )
            {
                if ( !arg.isEmpty() )
                {
                    args.add( arg );
                }
            }
        }

        return args.toArray( new String[0] );
    }
}
