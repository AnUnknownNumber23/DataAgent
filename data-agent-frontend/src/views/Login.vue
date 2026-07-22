<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-left">
        <div class="brand">
          <h1>DataAgent</h1>
          <p>企业级智能数据分析平台</p>
        </div>
        <div class="features">
          <div class="feature-item">
            <span class="dot"></span>
            <span>自然语言转 SQL，零门槛数据查询</span>
          </div>
          <div class="feature-item">
            <span class="dot"></span>
            <span>AI 深度分析 + 可视化报告自动生成</span>
          </div>
          <div class="feature-item">
            <span class="dot"></span>
            <span>多数据源接入，企业级安全保障</span>
          </div>
        </div>
      </div>
      <div class="login-right">
        <div class="tab-switch">
          <span :class="{ active: isLogin }" @click="isLogin = true">登录</span>
          <span :class="{ active: !isLogin }" @click="isLogin = false">注册</span>
        </div>

        <el-form :model="form" class="login-form" @keyup.enter="handleSubmit">
          <el-form-item>
            <el-input v-model="form.username" placeholder="用户名" size="large" clearable>
              <template #prefix>
                <span class="input-icon">👤</span>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-input v-model="form.password" type="password" placeholder="密码" size="large"
              show-password>
              <template #prefix>
                <span class="input-icon">🔒</span>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item v-if="!isLogin">
            <el-input v-model="form.email" placeholder="邮箱（选填）" size="large" clearable>
              <template #prefix>
                <span class="input-icon">📧</span>
              </template>
            </el-input>
          </el-form-item>

          <el-button type="primary" size="large" class="submit-btn" @click="handleSubmit"
            :loading="loading" round>
            {{ isLogin ? '登 录' : '注 册' }}
          </el-button>
        </el-form>

        <p class="demo-hint">测试账号：admin / admin123</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import axios from 'axios';

const router = useRouter();
const loading = ref(false);
const isLogin = ref(true);
const form = reactive({ username: '', password: '', email: '' });

const handleSubmit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }
  loading.value = true;
  try {
    const url = isLogin.value ? '/api/auth/login' : '/api/auth/register';
    const res = await axios.post(url, form);
    localStorage.setItem('token', res.data.data.token);
    localStorage.setItem('username', form.username);
    ElMessage.success(isLogin.value ? '登录成功' : '注册成功');
    router.push('/agents');
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  display: flex;
  width: 800px;
  min-height: 480px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: #fff;
  padding: 50px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand h1 {
  font-size: 32px;
  margin: 0 0 8px;
  font-weight: 700;
}

.brand p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0 0 40px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
}

.dot {
  width: 6px;
  height: 6px;
  background: #667eea;
  border-radius: 50%;
  flex-shrink: 0;
}

.login-right {
  flex: 1;
  padding: 50px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.tab-switch {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
  font-size: 18px;
  font-weight: 600;
  color: #999;
  cursor: pointer;
}

.tab-switch .active {
  color: #333;
  border-bottom: 2px solid #667eea;
  padding-bottom: 4px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.submit-btn {
  width: 100%;
  margin-top: 10px;
  height: 44px;
  font-size: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
}

.demo-hint {
  text-align: center;
  color: #999;
  font-size: 12px;
  margin-top: 20px;
}

.input-icon {
  font-size: 16px;
}
</style>
