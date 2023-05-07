package com.varcog.reconocimientofacial.service;

import com.varcog.reconocimientofacial.constants.ReconocimientoConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconocimientoService implements ReconocimientoServiceInterface {

    private final CascadeClassifier faceDetector;

    @Autowired
    public ReconocimientoService() throws IOException {
        OpenCV.loadShared();
        // Carga el clasificador de Haar Cascade para la detección de rostros
        Resource resource = new ClassPathResource("haarcascade_frontalface_alt.xml");
        String xmlFilePath = resource.getFile().getAbsolutePath();
        faceDetector = new CascadeClassifier(xmlFilePath);
    }

    @Override
    public byte[] detectFaces(byte[] imageByte) throws IOException {
        // Carga la imagen enviada por el cliente
        Mat image = Imgcodecs.imdecode(new MatOfByte(imageByte), Imgcodecs.IMREAD_UNCHANGED);

        // Detecta los rostros en la imagen
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);

        // Dibuja un rectángulo alrededor de cada rostro detectado
        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(image, rect.tl(), rect.br(), new Scalar(0, 255, 0), 3);
        }

        // Convierte la imagen procesada a un arreglo de bytes para que pueda ser enviada como respuesta HTTP

        // Convertir imagen a arreglo de bytes
        MatOfByte output = new MatOfByte();
        Imgcodecs.imencode(".jpg", image, output);
        InputStream inputStream = new ByteArrayInputStream(output.toArray());
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(output.toArray());
//        byte[] imageBytes = output.toArray();
        return inputStream.readAllBytes();
    }

    @Override
    public String compareFaces(byte[] image1Byte, byte[] image2Byte) {
        // Cargar imágenes
        Mat inputMat = Imgcodecs.imdecode(new MatOfByte(image1Byte), Imgcodecs.IMREAD_COLOR);
        Mat referenceMat = Imgcodecs.imdecode(new MatOfByte(image2Byte), Imgcodecs.IMREAD_COLOR);

        MatOfRect referenceFaceDetections = new MatOfRect();
        faceDetector.detectMultiScale(referenceMat, referenceFaceDetections);

        MatOfRect inputFaceDetections = new MatOfRect();
        faceDetector.detectMultiScale(inputMat, inputFaceDetections);

        // Verificar si hay exactamente un rostro detectado en cada imagen
        if (referenceFaceDetections.toArray().length != 1 || inputFaceDetections.toArray().length != 1) {
            return ReconocimientoConstants.NO_ROSTRO;
        }

        // Recortar los rostros de ambas imágenes
        Rect referenceFaceRect = referenceFaceDetections.toArray()[0];
        Mat referenceFace = new Mat(referenceMat, referenceFaceRect);

        Rect inputFaceRect = inputFaceDetections.toArray()[0];
        Mat inputFace = new Mat(inputMat, inputFaceRect);

        // Redimensionar los rostros a la misma altura y ancho
        int newHeight = 200;
        int newWidth = 200;

        Imgproc.resize(referenceFace, referenceFace, new Size(newWidth, newHeight));
        Imgproc.resize(inputFace, inputFace, new Size(newWidth, newHeight));

        // Calcular la diferencia entre los dos rostros
        Mat difference = new Mat();
        Core.absdiff(referenceFace, inputFace, difference);

        // Calcular el promedio de la diferencia
        Scalar meanDifference = Core.mean(difference);

        // Comparar la diferencia con un umbral
        double threshold = 30.0;

        if (meanDifference.val[0] < threshold) {
            return ReconocimientoConstants.SIMILARES;
        } else {
            return ReconocimientoConstants.DIFERENTES;
        }
    }
}
