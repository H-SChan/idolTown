package com.example.idoltown;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
class MainController {

    private final Repo repo;

    private final RestTemplate restTemplate = new RestTemplate();
    private final JSONParser jsonParser = new JSONParser();

    @GetMapping("/display/{fileName}")
    public ResponseEntity<Resource> showImg(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource;
        Path path = Paths.get("static/").toAbsolutePath().normalize();
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

    @GetMapping("/ranking")
    public List<Ranking> getRanking() {
        return repo.findAllByImgUrlOrderByPoint().orElseGet(() -> null);
    }

    @Transactional
    @PatchMapping("/win")
    public String win(@RequestBody String who) {
        Ranking winner = repo.findByName(who).orElseThrow(
                () -> new IllegalArgumentException("없는놈")
        );
        winner.setPoint(winner.getPoint() + 1);
        return "성공";
    }

    @Transactional
    @PutMapping("/save")
    public void save(@RequestBody String json) {
        try {
            JSONArray dataArr = (JSONArray) jsonParser.parse(json);

            for (Object o : dataArr) {
                JSONObject object = (JSONObject) o;
                repo.save(new Ranking((String) object.get("name"), (String) object.get("src"), 0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

@Getter
@Setter
@Entity
class Ranking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private String imgUrl;

    @ColumnDefault("0")
    private Integer point;

    private String name;

    public Ranking() {}

    public Ranking(String name, String imgUrl, int num) {
        this.name = name;
        this.imgUrl = imgUrl;
        this.point = num;
    }
}

interface Repo extends JpaRepository<Ranking, Long> {
    @Query("select r from Ranking r order by r.point")
    Optional<List<Ranking>> findAllByImgUrlOrderByPoint();

    Optional<Ranking> findByName(String name);
}
