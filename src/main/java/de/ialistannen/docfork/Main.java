package de.ialistannen.docfork;

import de.ialistannen.docfork.commands.DocCommand;
import de.ialistannen.docfork.commands.UpdateSlashesCommand;
import de.ialistannen.docfork.commands.system.Executor;
import de.ialistannen.javadocapi.querying.FuzzyElementQuery;
import de.ialistannen.javadocapi.storage.AggregatedElementLoader;
import de.ialistannen.javadocapi.storage.ConfiguredGson;
import de.ialistannen.javadocapi.storage.ElementLoader;
import de.ialistannen.javadocapi.storage.SqliteStorage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;

public class Main {

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Usage: <config file>");
      System.exit(1);
      return;
    }

    Config config = ConfiguredGson.create()
        .fromJson(Files.readString(Path.of(args[0])), Config.class);

    List<ElementLoader> storages = new ArrayList<>();
    for (Database database : config.getDatabases()) {
      storages.add(new SqliteStorage(
              ConfiguredGson.create(),
              Path.of(database.getPath())
          )
      );
    }

    JDA jda = JDABuilder.createDefault(config.getToken())
        .addEventListeners(new Executor(List.of(
            new DocCommand(
                new FuzzyElementQuery(),
                new AggregatedElementLoader(storages)
            ),
            new UpdateSlashesCommand()
        )))
        .build()
        .setRequiredScopes("applications.commands")
        .awaitReady();

    System.out.println(jda.getInviteUrl(Permission.MESSAGE_WRITE));
  }

  private static class Config {

    private String token;
    private List<Database> databases;

    public Config(String token, List<Database> databases) {
      this.token = token;
      this.databases = databases;
    }

    public String getToken() {
      return token;
    }

    public List<Database> getDatabases() {
      return databases;
    }
  }

  private static class Database {

    private String path;

    public Database(String path) {
      this.path = path;
    }

    public String getPath() {
      return path;
    }
  }
}
