package top.jgroup;

import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import top.jgroup.controller.FileController;

import static io.javalin.apibuilder.ApiBuilder.crud;

public class Main {

    public static final String URL = "http://localhost:8989";

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.registerPlugin(new OpenApiPlugin(openApiConfig -> openApiConfig
                    .withDocumentationPath("/openapi.json")
                    .withDefinitionConfiguration((version, definition) -> {
                        definition.withInfo(info -> {
                            info.setTitle("SkinStorage");
                            info.setVersion("1.0.0");
                        });
                    })));

            config.registerPlugin(new SwaggerPlugin(swaggerConfig -> {
                swaggerConfig.setUiPath("/swagger");
                swaggerConfig.setDocumentationPath("/openapi.json");
            }));

            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> {
                it.reflectClientOrigin = true;
                it.allowCredentials = true;
                it.exposeHeader("x-server");
            }));
        });

        app.unsafeConfig().router.apiBuilder(() -> {
            crud("files/{user-id}", new FileController());
        });

        app.get("/", context -> context.redirect("/swagger"));


        app.start(Integer.parseInt(URL.split(":")[2]));

        System.out.println("Server started at " + URL);
        System.out.println("Ура авто деплой (не)работает!");
    }
}
