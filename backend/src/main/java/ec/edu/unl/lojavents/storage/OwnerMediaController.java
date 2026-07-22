package ec.edu.unl.lojavents.storage;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/propietario/imagenes")
public class OwnerMediaController {
    private final MediaStorageService storage;

    public OwnerMediaController(MediaStorageService storage) {
        this.storage = storage;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<String> upload(@RequestParam("files") List<MultipartFile> files) {
        return files.stream()
                .map(storage::storeVenueImage)
                .map(file -> "/api/v1/imagenes/" + file.id())
                .toList();
    }
}
