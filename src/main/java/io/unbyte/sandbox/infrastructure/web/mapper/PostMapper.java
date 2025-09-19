package io.unbyte.sandbox.infrastructure.web.mapper;

import io.unbyte.sandbox.domain.model.Post;
import io.unbyte.sandbox.infrastructure.web.response.PostResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Post domain to DTO conversion
 */
@Mapper(componentModel = "spring", uses = CommentMapper.class)
public interface PostMapper {
    
    /**
     * Map domain Post to response DTO
     * @param post domain post
     * @return response DTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "comments", target = "comments")
    PostResponseDto toResponseDto(Post post);
}
