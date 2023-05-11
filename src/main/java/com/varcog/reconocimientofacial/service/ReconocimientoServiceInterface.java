package com.varcog.reconocimientofacial.service;

import java.io.IOException;

public interface ReconocimientoServiceInterface {

    byte[] detectFaces(byte[] imageByte) throws IOException;

    String compareFaces(byte[] image1Byte, byte[] image2Byte) ;

    String compareFaces2(byte[] image1Byte, byte[] image2Byte);

    String compareFaces3(byte[] image1Byte, byte[] image2Byte) throws IOException;

    String compareFaces4(String urlImage1, String urlImage2) throws IOException;

    String compareFaces5(String urlImage1, String urlImage2) throws IOException;
}
