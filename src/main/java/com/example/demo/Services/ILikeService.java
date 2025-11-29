package com.example.demo.Services;

import com.example.demo.dto.LikeRequestDTO;
import com.example.demo.dto.LikeResponseDTO;
import reactor.core.publisher.Mono;

public interface ILikeService {
    Mono<LikeResponseDTO> createLike(LikeRequestDTO likeRequestDTO);
    Mono<LikeResponseDTO> countLikesByTargetIdAndTargetType(String targetType , String targetId);
    Mono<LikeResponseDTO> countDisLikesByTargetIdAndTargetType(String targetType , String targetId);
    Mono<LikeResponseDTO> toggleLike(String targetId , String targetType ,Boolean isLike);
}
