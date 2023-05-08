package com.varcog.reconocimientofacial.controller;

import com.varcog.reconocimientofacial.service.ReconocimientoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.varcog.reconocimientofacial.constants.ReconocimientoConstants.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReconocimientoController {

    public final ReconocimientoService reconocimientoService;

    @CrossOrigin(origins = "*")
    @PostMapping(value = "/detectFaces", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> detectFaces(@RequestParam("file") MultipartFile file) throws IOException {
        byte[] imageBytes = reconocimientoService.detectFaces(file.getBytes());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageBytes);
    }

    @CrossOrigin(origins = "*")
    @PostMapping(value = "/compareFaces", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> compareFaces(@RequestParam("file1") MultipartFile file1, @RequestParam("file2") MultipartFile file2) throws IOException {
        String resultReconocimiento = reconocimientoService.compareFaces(file1.getBytes(), file2.getBytes());
        switch (resultReconocimiento) {
            case NO_ROSTRO:
                return new ResponseEntity<>("Error: una o ambas imágenes no contienen exactamente un rostro detectado.".getBytes(), HttpStatus.BAD_REQUEST);
            case SIMILARES:
                return new ResponseEntity<>("Los dos rostros son similares.".getBytes(), HttpStatus.OK);
            case DIFERENTES:
                return new ResponseEntity<>("Los dos rostros son diferentes.".getBytes(), HttpStatus.OK);
        }
        return new ResponseEntity<>("Error: Desconocido.".getBytes(), HttpStatus.BAD_REQUEST);
    }
}
