package io.unbyte.sandbox.infrastructure.web.mapper;

import io.unbyte.sandbox.domain.model.Comment;
import io.unbyte.sandbox.infrastructure.web.response.CommentResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Comment domain to DTO conversion
 */
@Mapper(componentModel = "spring")
public interface CommentMapper {

    /**
     * Map domain Comment to response DTO
     * @param comment domain comment
     * @return response DTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "body", target = "body")
    CommentResponseDto toResponseDto(Comment comment);
}
