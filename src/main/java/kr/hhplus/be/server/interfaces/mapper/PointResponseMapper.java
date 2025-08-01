package kr.hhplus.be.server.interfaces.mapper;

import kr.hhplus.be.server.domain.model.Point;
import kr.hhplus.be.server.interfaces.dto.response.PointResponse;

public class PointResponseMapper {
    public static PointResponse toPointResponse(Point point) {
        return new PointResponse(point.getUserId(), point.getBalance());
    }
}
