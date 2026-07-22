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
package com.alibaba.cloud.ai.dataagent.service.aimodelconfig;

import com.alibaba.cloud.ai.dataagent.enums.ModelType;
import com.alibaba.cloud.ai.dataagent.converter.ModelConfigConverter;
import com.alibaba.cloud.ai.dataagent.dto.ModelConfigDTO;
import com.alibaba.cloud.ai.dataagent.util.AesUtil;
import com.alibaba.cloud.ai.dataagent.entity.ModelConfig;
import com.alibaba.cloud.ai.dataagent.mapper.ModelConfigMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.alibaba.cloud.ai.dataagent.converter.ModelConfigConverter.toEntity;

@Slf4j
@Service
@AllArgsConstructor
public class ModelConfigDataServiceImpl implements ModelConfigDataService {

    private final ModelConfigMapper modelConfigMapper;

    @Override
    public ModelConfig findById(Integer id) {
        return modelConfigMapper.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    @CacheEvict(value = "modelConfigs", allEntries = true)
    public void switchActiveStatus(Integer id, ModelType type) {
        modelConfigMapper.deactivateOthers(type.getCode(), id);
        ModelConfig entity = modelConfigMapper.findById(id);
        if (entity != null) {
            entity.setIsActive(true);
            entity.setUpdatedTime(LocalDateTime.now());
            modelConfigMapper.updateById(entity);
        }
    }

    @Override
    @Cacheable("modelConfigs")
    public List<ModelConfigDTO> listConfigs() {
        return modelConfigMapper.findAll().stream().map(ModelConfigConverter::toDTO).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "modelConfigs", allEntries = true)
    public void addConfig(ModelConfigDTO dto) {
        clean(dto);
        modelConfigMapper.insert(toEntity(dto));
    }

    private void clean(ModelConfigDTO dto) {
        dto.setModelName(dto.getModelName().trim());
        dto.setBaseUrl(dto.getBaseUrl().trim());
        dto.setApiKey(dto.getApiKey().trim());
        if (dto.getCompletionsPath() != null) {
            dto.setCompletionsPath(dto.getCompletionsPath().trim());
        }
        if (dto.getEmbeddingsPath() != null) {
            dto.setEmbeddingsPath(dto.getEmbeddingsPath().trim());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    @CacheEvict(value = "modelConfigs", allEntries = true)
    public ModelConfig updateConfigInDb(ModelConfigDTO dto) {
        clean(dto);
        ModelConfig entity = modelConfigMapper.findById(dto.getId());
        if (entity == null) {
            throw new RuntimeException("配置不存在");
        }
        if (!entity.getModelType().getCode().equals(dto.getModelType()))
            throw new RuntimeException("模型类型不允许修改");
        mergeDtoToEntity(dto, entity);
        entity.setUpdatedTime(LocalDateTime.now());
        modelConfigMapper.updateById(entity);
        return entity;
    }

    private static void mergeDtoToEntity(ModelConfigDTO dto, ModelConfig oldEntity) {
        oldEntity.setProvider(dto.getProvider());
        oldEntity.setBaseUrl(dto.getBaseUrl());
        oldEntity.setModelName(dto.getModelName());
        oldEntity.setTemperature(dto.getTemperature());
        oldEntity.setMaxTokens(dto.getMaxTokens());
        oldEntity.setCompletionsPath(dto.getCompletionsPath());
        oldEntity.setEmbeddingsPath(dto.getEmbeddingsPath());
        oldEntity.setUpdatedTime(LocalDateTime.now());
        oldEntity.setProxyEnabled(dto.getProxyEnabled());
        oldEntity.setProxyHost(dto.getProxyHost());
        oldEntity.setProxyPort(dto.getProxyPort());
        oldEntity.setProxyUsername(dto.getProxyUsername());
        oldEntity.setProxyPassword(dto.getProxyPassword());
        if (dto.getApiKey() != null && !dto.getApiKey().contains("****")) {
            oldEntity.setApiKey(AesUtil.encrypt(dto.getApiKey()));
        }
    }

    @Override
    @CacheEvict(value = "modelConfigs", allEntries = true)
    public void deleteConfig(Integer id) {
        ModelConfig entity = modelConfigMapper.findById(id);
        if (entity == null) {
            throw new RuntimeException("配置不存在");
        }
        if (Boolean.TRUE.equals(entity.getIsActive())) {
            throw new RuntimeException("该配置当前正在使用中，无法删除！请先激活其他配置，再进行删除操作。");
        }
        entity.setIsDeleted(1);
        entity.setUpdatedTime(LocalDateTime.now());
        int updated = modelConfigMapper.updateById(entity);
        if (updated == 0) {
            throw new RuntimeException("删除失败");
        }
    }

    @Override
    @Cacheable(value = "activeModelConfig", key = "#modelType.code")
    public ModelConfigDTO getActiveConfigByType(ModelType modelType) {
        ModelConfig entity = modelConfigMapper.selectActiveByType(modelType.getCode());
        if (entity == null) {
            log.warn("Activation model configuration of type [{}] not found, attempting to downgrade...", modelType);
            return null;
        }
        return ModelConfigConverter.toDTOForInternal(entity);
    }
}
