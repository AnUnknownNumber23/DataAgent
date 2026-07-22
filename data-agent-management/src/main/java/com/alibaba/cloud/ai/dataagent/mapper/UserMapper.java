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
package com.alibaba.cloud.ai.dataagent.mapper;

import com.alibaba.cloud.ai.dataagent.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM dataagent_user WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM dataagent_user WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO dataagent_user (username, password, email, create_time, update_time) " +
            "VALUES (#{username}, #{password}, #{email}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("SELECT COUNT(*) FROM dataagent_user WHERE username = #{username}")
    int countByUsername(String username);
}
