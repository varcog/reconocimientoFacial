package com.varcog.reconocimientofacial.service;

import java.io.IOException;

public interface ReconocimientoServiceInterface {

    byte[] detectFaces(byte[] imageByte) throws IOException;

    String compareFaces(byte[] image1Byte, byte[] image2Byte) ;

}
