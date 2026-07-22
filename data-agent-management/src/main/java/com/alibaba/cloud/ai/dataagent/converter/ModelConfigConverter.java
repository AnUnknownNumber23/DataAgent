/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.dataagent.converter;

import com.alibaba.cloud.ai.dataagent.dto.ModelConfigDTO;
import com.alibaba.cloud.ai.dataagent.entity.ModelConfig;
import com.alibaba.cloud.ai.dataagent.enums.ModelType;
import com.alibaba.cloud.ai.dataagent.util.AesUtil;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public class ModelConfigConverter {

    /**
     * Entity -> DTO (返回前端展示，Key 脱敏)
     */
    public static ModelConfigDTO toDTO(ModelConfig entity) {
        if (entity == null) {
            return null;
        }
        String apiKey = entity.getApiKey();
        String displayKey = null;
        if (StringUtils.hasText(apiKey)) {
            String decrypted = AesUtil.decrypt(apiKey);
            String prefix = decrypted.startsWith("sk-") ? "sk-" : "";
            String suffix = decrypted.substring(Math.max(0, decrypted.length() - 4));
            displayKey = prefix + "****" + suffix;
        }
        return ModelConfigDTO.builder()
            .id(entity.getId())
            .provider(entity.getProvider())
            .baseUrl(entity.getBaseUrl())
            .modelName(entity.getModelName())
            .temperature(entity.getTemperature())
            .maxTokens(entity.getMaxTokens())
            .isActive(entity.getIsActive())
            .apiKey(displayKey)
            .modelType(entity.getModelType().getCode())
            .completionsPath(entity.getCompletionsPath())
            .embeddingsPath(entity.getEmbeddingsPath())
            .proxyEnabled(entity.getProxyEnabled())
            .proxyHost(entity.getProxyHost())
            .proxyPort(entity.getProxyPort())
            .proxyUsername(entity.getProxyUsername())
            .proxyPassword(entity.getProxyPassword())
            .build();
    }

    /**
     * DTO -> Entity (新增/修改时加密存储)
     */
    public static ModelConfig toEntity(ModelConfigDTO dto) {
        Assert.notNull(dto, "ModelConfigDTO cannot be null.");
        ModelConfig entity = new ModelConfig();
        entity.setId(dto.getId());
        entity.setProvider(dto.getProvider());
        entity.setBaseUrl(dto.getBaseUrl());
        // 加密存储：只有 DTO 传了明文 Key 才加密，否则保持原值
        if (StringUtils.hasText(dto.getApiKey()) && !dto.getApiKey().startsWith("****")) {
            entity.setApiKey(AesUtil.encrypt(dto.getApiKey()));
        }
        entity.setModelName(dto.getModelName());
        entity.setTemperature(dto.getTemperature());
        entity.setMaxTokens(dto.getMaxTokens());
        entity.setModelType(ModelType.fromCode(dto.getModelType()));
        entity.setCompletionsPath(dto.getCompletionsPath());
        entity.setEmbeddingsPath(dto.getEmbeddingsPath());
        entity.setProxyEnabled(dto.getProxyEnabled());
        entity.setProxyHost(dto.getProxyHost());
        entity.setProxyPort(dto.getProxyPort());
        entity.setProxyUsername(dto.getProxyUsername());
        entity.setProxyPassword(dto.getProxyPassword());
        entity.setIsActive(false);
        entity.setIsDeleted(0);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        return entity;
    }

    /**
     * Entity -> DTO 供内部调用（解密返回完整 Key）
     */
    public static ModelConfigDTO toDTOForInternal(ModelConfig entity) {
        ModelConfigDTO dto = toDTO(entity);
        if (dto != null && StringUtils.hasText(entity.getApiKey())) {
            dto.setApiKey(AesUtil.decrypt(entity.getApiKey()));
        }
        return dto;
    }
}
