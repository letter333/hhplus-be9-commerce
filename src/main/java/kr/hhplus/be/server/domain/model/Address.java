package kr.hhplus.be.server.domain.model;

public record Address(
        String shippingAddress1,
        String shippingAddress2,
        String shippingZipCode
) {

}
