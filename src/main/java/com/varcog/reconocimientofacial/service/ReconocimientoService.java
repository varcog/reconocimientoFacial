package com.varcog.reconocimientofacial.service;

import com.varcog.reconocimientofacial.constants.ReconocimientoConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_dnn;
import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
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
        System.out.println("compareFaces");
        // Cargar imagenes
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

        // Recortar los rostros de ambas imagenes
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
    @Override
    public String compareFaces2(byte[] image1Byte, byte[] image2Byte) {
        System.out.println("compareFaces2");
        // Cargar imagenes
        Mat inputMat = Imgcodecs.imdecode(new MatOfByte(image1Byte), Imgcodecs.IMREAD_UNCHANGED);
        Mat referenceMat = Imgcodecs.imdecode(new MatOfByte(image2Byte), Imgcodecs.IMREAD_UNCHANGED);

//        Mat inputMat = new Mat();
//        Mat referenceMat = new Mat();
//
//        Imgproc.cvtColor(inputMatRead, inputMat, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.cvtColor(referenceMatRead, referenceMat, Imgproc.COLOR_BGR2GRAY);


        MatOfRect referenceFaceDetections = new MatOfRect();
        faceDetector.detectMultiScale(referenceMat, referenceFaceDetections);

        MatOfRect inputFaceDetections = new MatOfRect();
        faceDetector.detectMultiScale(inputMat, inputFaceDetections);

        // Verificar si hay exactamente un rostro detectado en cada imagen
        if (referenceFaceDetections.toArray().length != 1 || inputFaceDetections.toArray().length != 1) {
            return ReconocimientoConstants.NO_ROSTRO;
        }

        // Recortar los rostros de ambas imagenes
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

        // Convertir la diferencia en escala de grises
        Mat grayDiff = new Mat();
        Imgproc.cvtColor(difference, grayDiff, Imgproc.COLOR_BGR2GRAY);
        double similarityScore = Core.mean(grayDiff).val[0];

        System.out.println("Puntuación de similitud color: " + meanDifference.val[0] );
        System.out.println("Puntuación de similitud gray: " + similarityScore);


        // Visualizar las imágenes y la diferencia
        Imgcodecs.imwrite("referenceFace.jpg", referenceFace);
        Imgcodecs.imwrite("inputFace.jpg", inputFace);
        Imgcodecs.imwrite("diferencia.jpg", difference);
        Imgcodecs.imwrite("diferenciagray.jpg", grayDiff);

        // Comparar la diferencia con un umbral
        double threshold = 60.0;

        if (meanDifference.val[0] < threshold) {
            return ReconocimientoConstants.SIMILARES;
        } else {
            return ReconocimientoConstants.DIFERENTES;
        }
    }
    @Override
    public String compareFaces3(byte[] image1Byte, byte[] image2Byte) throws IOException {
        System.out.println("compareFaces3");

        // Cargar la biblioteca OpenCV y el modelo preentrenado de FaceNet
        Loader.load(opencv_dnn.class);
        Net net = Dnn.readNetFromTorch(getModelPath());

        // Cargar las imágenes de los rostros a comparar
        Mat image1 = Imgcodecs.imdecode(new MatOfByte(image1Byte), Imgcodecs.IMREAD_UNCHANGED);
        Mat image2 = Imgcodecs.imdecode(new MatOfByte(image2Byte), Imgcodecs.IMREAD_UNCHANGED);

        // Preprocesar las imágenes de los rostros para el modelo
        Mat preprocessedFace1 = preprocessFace(image1);
        Mat preprocessedFace2 = preprocessFace(image2);

        // Calcular las características faciales de los rostros utilizando el modelo
        FloatPointer features1 = extractFeatures(net, preprocessedFace1);
        FloatPointer features2 = extractFeatures(net, preprocessedFace2);

        // Comparar las características faciales de los rostros
        double similarityScore = compareFeatures(features1, features2);

        // Mostrar la puntuación de similitud
        System.out.println("Puntuación de similitud: " + similarityScore);

        double threshold = 0.7;

        if (similarityScore < threshold) {
            return ReconocimientoConstants.DIFERENTES;
        }

        return ReconocimientoConstants.SIMILARES;
    }

    @Override
    public String compareFaces4(String urlImage1, String urlImage2) throws IOException {
        System.out.println("compareFaces4");

        // Cargar la biblioteca OpenCV y el modelo preentrenado de FaceNet
        Loader.load(opencv_dnn.class);
        Net net = Dnn.readNetFromTorch(getModelPath());

        // Cargar las imágenes de los rostros a comparar
        Mat image1 = Imgcodecs.imread(urlImage1);
        Mat image2 = Imgcodecs.imread(urlImage2);

        // Preprocesar las imágenes de los rostros para el modelo
        Mat preprocessedFace1 = preprocessFace(image1);
        Mat preprocessedFace2 = preprocessFace(image2);

        // Calcular las características faciales de los rostros utilizando el modelo
        FloatPointer features1 = extractFeatures(net, preprocessedFace1);
        FloatPointer features2 = extractFeatures(net, preprocessedFace2);

        // Comparar las características faciales de los rostros
        double similarityScore = compareFeatures(features1, features2);

        // Mostrar la puntuación de similitud
        System.out.println("Puntuación de similitud: " + similarityScore);

        double threshold = 0.7;

        if (similarityScore < threshold) {
            return ReconocimientoConstants.DIFERENTES;
        }

        return ReconocimientoConstants.SIMILARES;
    }

    @Override
    public String compareFaces5(String urlImage1, String urlImage2) throws IOException {
        System.out.println("compareFaces5");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Net net = Dnn.readNetFromTorch(getModelPath());
        Mat feature1 = process(net, Imgcodecs.imread(urlImage1));
        Mat feature2 = process(net, Imgcodecs.imread(urlImage2));
        double dist  = Core.norm(feature1,  feature2);
        System.out.println("similitud " + dist);
        if (dist < 0.6) {
            return ReconocimientoConstants.SIMILARES;
        }
        return ReconocimientoConstants.DIFERENTES;
    }

    public Mat process(Net net, Mat img) {
        Mat inputBlob = Dnn.blobFromImage(img, 1./255,
                new Size(96,96), new Scalar(0,0,0), true, false);
        net.setInput(inputBlob);
        return net.forward().clone();
    }


    private String getModelPath() throws IOException {
        Resource resource = new ClassPathResource("nn4.small2.v1.t7");
        return resource.getFile().getAbsolutePath();
    }

    private Mat preprocessFace(Mat face) {
        // Redimensionar la imagen del rostro a un tamaño específico requerido por el modelo
        Size targetSize = new Size(96, 96);
        Mat resizedFace = new Mat();
        Imgproc.resize(face, resizedFace, targetSize);

        // Preprocesar la imagen del rostro para el modelo
        Mat preprocessedFace = new Mat();
        resizedFace.convertTo(preprocessedFace, opencv_core.CV_32F, 1.0 / 255);

        return preprocessedFace;
    }

    private FloatPointer extractFeatures(Net net, Mat face) {
        // Convertir la imagen del rostro en un blob para el modelo
        Mat blob = Dnn.blobFromImage(face, 1.0, new Size(96, 96), Scalar.all(0), false, false);

        // Pasar el blob al modelo para obtener las características faciales
        net.setInput(blob);
        Mat features = net.forward();

        // Obtener los datos de las características faciales como un arreglo de tipo float[]
        float[] featuresArray = new float[features.cols()];
        features.get(0, 0, featuresArray);

        // Crear un FloatPointer a partir del arreglo float[]
        FloatPointer featuresPtr = new FloatPointer(featuresArray);

        return featuresPtr;
    }


    private double compareFeatures(FloatPointer features1, FloatPointer features2) {
        double similarityScore = 0.0;
        for (int i = 0; i < features1.capacity(); i++) {
            double diff = features1.get(i) - features2.get(i);
            similarityScore += diff * diff;
        }
        similarityScore = Math.sqrt(similarityScore);
        return similarityScore;
    }
}
