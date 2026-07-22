package ec.edu.unl.lojavents.storage;

import com.mongodb.client.gridfs.model.GridFSFile;
import ec.edu.unl.lojavents.common.api.ApiException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class MediaStorageService {

    private static final long MAX_DOCUMENT_SIZE = 5 * 1024 * 1024;
    private static final long MAX_IMAGE_SIZE = 8 * 1024 * 1024;
    private static final List<String> DOCUMENT_TYPES = List.of("application/pdf", "image/png", "image/jpeg");
    private static final List<String> IMAGE_TYPES = List.of("image/png", "image/jpeg", "image/webp");

    private final GridFsTemplate gridFsTemplate;

    public MediaStorageService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    public StoredFile storeOwnerDocument(MultipartFile file) {
        validate(file, DOCUMENT_TYPES, MAX_DOCUMENT_SIZE, "El documento debe ser PDF, PNG o JPG y pesar hasta 5 MB.");
        return store(file, "OWNER_REQUEST_DOCUMENT", null);
    }

    public StoredFile storeVenueImage(MultipartFile file) {
        validate(file, IMAGE_TYPES, MAX_IMAGE_SIZE, "La imagen debe ser PNG, JPG o WEBP y pesar hasta 8 MB.");
        return store(file, "VENUE_IMAGE", null);
    }

    public String storeSeedImage(String seedKey, String fileName, String contentType, InputStream content) {
        GridFSFile existing = gridFsTemplate.findOne(Query.query(
                Criteria.where("metadata.seedKey").is(seedKey)
        ));
        if (existing != null) {
            return existing.getObjectId().toHexString();
        }
        ObjectId id = gridFsTemplate.store(content, fileName, contentType,
                new Document("kind", "VENUE_IMAGE").append("seedKey", seedKey));
        return id.toHexString();
    }

    public GridFsResource read(String id) {
        try {
            GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(new ObjectId(id))));
            if (file == null) {
                throw notFound();
            }
            return gridFsTemplate.getResource(file);
        } catch (IllegalArgumentException exception) {
            throw notFound();
        }
    }

    private StoredFile store(MultipartFile file, String kind, String seedKey) {
        try (InputStream input = file.getInputStream()) {
            String contentType = file.getContentType();
            ObjectId id = gridFsTemplate.store(input, safeName(file.getOriginalFilename()), contentType,
                    new Document("kind", kind).append("seedKey", seedKey));
            return new StoredFile(id.toHexString(), safeName(file.getOriginalFilename()), contentType, file.getSize());
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "FILE_READ_ERROR", "No fue posible leer el archivo enviado.");
        }
    }

    private void validate(MultipartFile file, List<String> allowedTypes, long maxSize, String message) {
        if (file == null || file.isEmpty() || !allowedTypes.contains(file.getContentType()) || file.getSize() > maxSize) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_FILE", message);
        }
    }

    private String safeName(String name) {
        return name == null || name.isBlank() ? "archivo" : name.replaceAll("[\\r\\n]", "_");
    }

    private ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "El archivo solicitado no existe.");
    }
}
