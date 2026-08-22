package com.gamifiedstudyhub.backend.common.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Shared MapStruct settings for future generated mappers. A mapper opts in with
 * {@code @Mapper(config = CentralMapperConfig.class)} to get Spring-injectable beans,
 * constructor injection, and a compile-time error on any unmapped target property.
 *
 * <p>MapStruct is pre-wired (processor on the compile classpath) but not yet used —
 * existing hand-written mappers are unaffected.
 */
@MapperConfig(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface CentralMapperConfig {
}
