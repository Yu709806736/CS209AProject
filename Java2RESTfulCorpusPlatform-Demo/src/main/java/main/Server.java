package main;

import dao.TextDao;
import io.javalin.Javalin;
import io.swagger.v3.oas.models.info.Info;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.ReDocOptions;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import service.TextService;

public class Server {
    /**
     * Main method for Server, act as a Controller in RESTful architecture.
     * Initialize table "documents" with three columns:
     *     not null and unique "md5" column whose datatype is `text` and which stands for the md5 sum of each file,
     *     integer column "length" stored the length of the content, and
     *     not null "content" column whose datatype is `text` and which stands for the column of each file.
     * Can also control TextDao(Storage) and TextService(Analyzer).
     * Receive HTTP commands from client.
     * @param args: empty
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws ClassNotFoundException {
        //TODO:connect database - finished
        Class.forName("org.sqlite.JDBC");
        Sql2o sql2o = new Sql2o("jdbc:sqlite:Doc.db", null, null);
        String initSql = "create table if not exists \"documents\"(\n" +
                " \"md5\" text not null unique,\n" +
                " \"len\" integer,\n" +
                " \"content\" text not null\n" +
                ")";
        try (Connection con = sql2o.open()) {
            con.createQuery(initSql).executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }


        TextDao dao = new TextDao();
        TextService service = new TextService(dao);

        Javalin app = Javalin.create(config -> {
            config.registerPlugin(getConfiguredOpenApiPlugin());
        }).start(7001);
        app.get("/", ctx -> ctx.result("Welcome to RESTful Corpus Platform"));
        // handle file table
        app.get("/files", service::handleList);
        // handle exist
        app.get("/files/:md5/exists", service::handleExists);
        // handle upload
        app.post("/files/:md5", service::handleUpload);
        // handle compare
        app.get("/files/:md51/compare/:md52", service::handleCompare);
        // handle download
        app.get("/files/:md5", service::handleDownload);
    }

    /**
     * Configure API.
     * @return
     */
    private static OpenApiPlugin getConfiguredOpenApiPlugin() {
        Info info = new Info().version("1.0").description("RESTful Corpus Platform API");
        OpenApiOptions options = new OpenApiOptions(info)
                .activateAnnotationScanningFor("cn.edu.sustech.java2.RESTfulCorpusPlatform")
                .path("/swagger-docs") // endpoint for OpenAPI json
                .swagger(new SwaggerOptions("/swagger-ui")); // endpoint for swagger-ui
//                .reDoc(new ReDocOptions("/redoc")); // endpoint for redoc
        return new OpenApiPlugin(options);
    }
}