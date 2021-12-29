package com.example.idoltown;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.function.EntityResponse;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class MainController {

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> showImg(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource;
        Path path = Paths.get("static/").toAbsolutePath().normalize();
        System.out.println(path);
        try {
            Path filePath = path.resolve(fileName).normalize();
            resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
                return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
            } else throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "없는 파일");
        } catch (Exception e) {
            e.printStackTrace();
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }
    }
}
