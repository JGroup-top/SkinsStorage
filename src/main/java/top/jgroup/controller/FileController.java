package top.jgroup.controller;

import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import io.javalin.openapi.*;
import io.javalin.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.jgroup.Main;
import top.jgroup.utlis.MessageUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

public class FileController implements CrudHandler {

    private static final String DIR_NAME = "files";

    public FileController() {
        File dir = new File(DIR_NAME);
        if (!dir.exists()) {
            if (dir.mkdirs())
                System.out.println("Directory " + DIR_NAME + " created successfully.");
            else
                System.err.println("Failed to create directory " + DIR_NAME + ".");
        }
    }

    @Override
    @OpenApi(
            path = "/files",
            methods = HttpMethod.POST,
            summary = "Upload a new file",
            description = "Uploads an image file to the skins directory",
            tags = {"File"},
            requestBody = @OpenApiRequestBody(
                    content = {
                            @OpenApiContent(
                                    mimeType = "multipart/form-data",
                                    type = "object",
                                    properties = {
                                            @OpenApiContentProperty(
                                                    name = "file",
                                                    type = "string",
                                                    format = "binary"
                                            )
                                    }
                            )
                    },
                    description = "Файл изображения (.png, .jpg, .jpeg)",
                    required = true
            ),
            responses = {
                    @OpenApiResponse(status = "200", description = "File uploaded successfully",
                            content = @OpenApiContent(
                                    type = "application/json",
                                    example = "{\"success\":true, \"url\":\"http://localhost:7000/files/yourfile.png\"}"
                            )),
                    @OpenApiResponse(status = "400", description = "No file uploaded",
                            content = @OpenApiContent(
                                    type = "application/json",
                                    example = "{\"success\":false, \"message\":\"File not found\"}"
                            )),
                    @OpenApiResponse(status = "415", description = "Unsupported file type",
                            content = @OpenApiContent(
                                    type = "application/json",
                                    example = "{\"success\":false, \"message\":\"Unsupported file type. Only PNG and JPEG are allowed.\"}"
                            ))
            }
    )
    public void create(@NotNull Context context) {
        saveImage(context);
    }

    @Override
    @OpenApi(
            path = "/files",
            methods = HttpMethod.GET,
            summary = "Get list of all files",
            description = "Returns a JSON array of all files with their names and URLs",
            tags = {"File"},
            responses = {
                    @OpenApiResponse(status = "200", description = "List of files",
                            content = @OpenApiContent(
                                    type = "application/json",
                                    example = "[{\"filename\":\"skin1.png\", \"url\":\"http://localhost:7000/skins/skin1.png\"}]"
                            )),
                    @OpenApiResponse(status = "200", description = "Empty list if no files",
                            content = @OpenApiContent(
                                    type = "application/json",
                                    example = "[]"
                            ))
            }
    )
    public void getAll(@NotNull Context context) {
        File dir = new File(DIR_NAME);

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            context.json(new Object[0]);
            return;
        }

        var result = new ArrayList<HashMap<String, String>>();
        for (File file : files) {
            if (file.isFile()) {
                HashMap<String, String> fileInfo = new HashMap<>();
                fileInfo.put("filename", file.getName());
                fileInfo.put("url", Main.URL + "/" + DIR_NAME + "/" + file.getName());
                result.add(fileInfo);
            }
        }

        context.status(200).json(result);
    }

    @Override
    @OpenApi(
            path = "/files/{filename}",
            methods = HttpMethod.GET,
            summary = "Download file by filename",
            description = "Returns the file content for the given filename",
            tags = {"File"},
            pathParams = {
                    @OpenApiParam(name = "filename", description = "Name of the file to download", required = true)
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "File returned",
                            content = @OpenApiContent(type = "application/octet-stream")),
                    @OpenApiResponse(status = "404", description = "File not found",
                            content = @OpenApiContent(
                                    type = "application/json",
                                    example = "{\"success\":false, \"message\":\"File not found\"}"
                            ))
            }
    )
    public void getOne(@NotNull Context context, @NotNull String filename) {
        File file = getImage(context, filename);
        if (file == null) return;

        try {
            context.status(200).result(new FileInputStream(file));
            String mimeType = Files.probeContentType(file.toPath());
            context.contentType(mimeType != null ? mimeType : "application/octet-stream");
            context.header("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
        } catch (IOException e) {
            context.status(404). json(MessageUtil.createJsonMessage(false, "File not found"));
        }
    }

    @Override
    @OpenApi(
            path = "/files/{filename}",
            methods = HttpMethod.PUT,
            summary = "Update file by filename",
            description = "Updates existing file by overwriting it with uploaded file",
            tags = {"File"},
            pathParams = {
                    @OpenApiParam(name = "filename", description = "Name of the file to update", required = true)
            },
            requestBody = @OpenApiRequestBody(
                    content = {
                            @OpenApiContent(
                                    mimeType = "multipart/form-data",
                                    type = "object",
                                    properties = {
                                            @OpenApiContentProperty(
                                                    name = "file",
                                                    type = "string",
                                                    format = "binary"
                                            )
                                    }
                            )
                    },
                    description = "Файл изображения (.png, .jpg, .jpeg)",
                    required = true
            ),
            responses = {
                    @OpenApiResponse(status = "200", description = "File updated successfully",
                            content = @OpenApiContent(
                                    type = "application/json",
                                    example = "{\"success\":true, \"url\":\"http://localhost:7000/skins/yourfile.png\"}"
                            )),
                    @OpenApiResponse(status = "400", description = "No file uploaded",
                            content = @OpenApiContent(
                                    type = "application/json",
                                    example = "{\"success\":false, \"message\":\"File not found\"}"
                            )),
                    @OpenApiResponse(status = "404", description = "File not found",
                            content = @OpenApiContent(
                                    type = "application/json",
                                    example = "{\"success\":false, \"message\":\"File not found\"}"
                            ))
            }
    )
    public void update(@NotNull Context context, @NotNull String filename) {
        File file = getImage(context, filename);
        if (file == null) return;

        saveImage(context);
    }

    @Override
    @OpenApi(
            path = "/files/{filename}",
            methods = HttpMethod.DELETE,
            summary = "Delete file by filename",
            description = "Deletes the file with the specified filename",
            tags = {"File"},
            pathParams = {
                    @OpenApiParam(name = "filename", description = "Name of the file to delete", required = true)
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "File deleted successfully",
                            content = @OpenApiContent(
                                    type = "application/json",
                                    example = "{\"success\":true, \"message\":\"File deleted successfully\"}"
                            )),
                    @OpenApiResponse(status = "404", description = "File not found",
                            content = @OpenApiContent(
                                    type = "application/json",
                                    example = "{\"success\":false, \"message\":\"File not found\"}"
                            )),
                    @OpenApiResponse(status = "500", description = "Failed to delete file",
                            content = @OpenApiContent(
                                    type = "application/json",
                                    example = "{\"success\":false, \"message\":\"Failed to delete file\"}"
                            ))
            }
    )
    public void delete(@NotNull Context context, @NotNull String filename) {
        File file = getImage(context, filename);
        if (file == null) return;

        if (file.delete()) {
            context.status(200).json(MessageUtil.createJsonMessage(true, "File deleted successfully"));
        } else {
            context.status(500).json(MessageUtil.createJsonMessage("Failed to delete file"));
        }
    }

    private void saveImage(@NotNull Context context) {
        UploadedFile uploadedFile = context.uploadedFile("file");
        if (uploadedFile == null) {
            context.status(400).json(MessageUtil.createJsonMessage("File not found"));
            return;
        }

        String contentType = uploadedFile.contentType();
        if (!isSupportedImageType(contentType)) {
            context.status(415).json(MessageUtil.createJsonMessage("Unsupported file type. Only PNG and JPEG are allowed."));
            return;
        }

        String filePath = DIR_NAME + "/" + uploadedFile.filename().toLowerCase();

        FileUtil.streamToFile(uploadedFile.content(), filePath);

        context.status(200).json(MessageUtil.createJsonMessage(true, "url", Main.URL + "/" + filePath));
    }

    @Nullable
    private static File getImage(@NotNull Context context, @NotNull String filename) {
        File directFile = new File(DIR_NAME + "/" + filename);
        if (directFile.exists() && directFile.isFile()) {
            return directFile;
        }

        String[] extensions = {".png", ".jpg", ".jpeg"};
        for (String ext : extensions) {
            File f = new File(DIR_NAME + "/" + filename + ext);
            if (f.exists() && f.isFile()) {
                return f;
            }
        }

        context.status(404).json(MessageUtil.createJsonMessage("File not found"));
        return null;
    }


    private boolean isSupportedImageType(String contentType) {
        return contentType != null &&
                (contentType.equalsIgnoreCase("image/png") ||
                        contentType.equalsIgnoreCase("image/jpeg"));
    }
}
