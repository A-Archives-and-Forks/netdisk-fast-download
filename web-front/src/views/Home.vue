<template>
  <div id="app" v-cloak  :class="{ 'dark-theme': isDarkMode }">
    <!-- <el-dialog
      v-model="showRiskDialog"
      title="使用本网站您应该同意"
      width="300px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      center
    >
      <div style="font-size:1.08em;line-height:1.8;">
        请勿在本平台分享、传播任何违法内容，包括但不限于：<br>
        违规视频、游戏外挂、侵权资源、涉政涉黄等。<br>        
      </div>
      <template #footer>
        <el-button type="primary" @click="ackRisk">我知道了</el-button>
      </template>
    </el-dialog> -->
    <!-- 顶部反馈栏（小号、灰色、无红边框） -->
    <div class="feedback-bar">
      <a href="https://github.com/qaiu/netdisk-fast-download/issues" target="_blank" rel="noopener" class="feedback-link mini">
        <i class="fas fa-bug feedback-icon"></i>
        反馈
      </a>
      <a href="https://github.com/qaiu/netdisk-fast-download" target="_blank" rel="noopener" class="feedback-link mini">
        <i class="fab fa-github feedback-icon"></i>
        源码
      </a>
      <a href="https://blog.qaiu.top" target="_blank" rel="noopener" class="feedback-link mini">
        <i class="fas fa-blog feedback-icon"></i>
        博客
      </a>
      <a href="https://blog.qaiu.top/archives/netdisk-fast-download-bao-ta-an-zhuang-jiao-cheng" target="_blank" rel="noopener" class="feedback-link mini">
        <i class="fas fa-server feedback-icon"></i>
        部署
      </a>
      <a href="javascript:void(0)" class="feedback-link mini donate-link" @click="showDonateDialog = true">
        <i class="fas fa-gift feedback-icon" style="color: #e74c3c;"></i>
        捐赠账号
      </a>
    </div>
    <el-row :gutter="20" style="margin-left: 0; margin-right: 0;">
      <el-card class="box-card">
        <div style="text-align: right; display: flex; justify-content: space-between; align-items: center;">
          <!-- 左侧认证配置按钮 -->
          <el-tooltip content="配置临时认证信息" placement="bottom">
            <el-button 
              :type="hasAuthConfig ? 'primary' : 'default'" 
              :class="{ 'auth-config-btn-active': hasAuthConfig }"
              circle 
              size="small" 
              @click="showAuthConfigDialog = true">
              <el-icon><Key /></el-icon>
            </el-button>
          </el-tooltip>
          <!-- 右侧暗色模式切换 -->
          <DarkMode @theme-change="handleThemeChange" />
        </div>
        <div class="demo-basic--circle">
          <div class="block" style="text-align: center;">
            <img :height="150" src="../../public/images/lanzou111.png" alt="lz">
          </div>
        </div>
        <!-- 项目简介移到卡片内 -->
        <div class="project-intro">
          <div class="intro-title">NFD网盘直链解析0.2.1</div>
          <div class="intro-desc">
            <div>支持网盘：蓝奏云、蓝奏云优享、小飞机盘、123云盘、奶牛快传、移动云空间、QQ邮箱云盘、QQ闪传等 <el-link style="color:#606cf5" href="https://github.com/qaiu/netdisk-fast-download?tab=readme-ov-file#%E7%BD%91%E7%9B%98%E6%94%AF%E6%8C%81%E6%83%85%E5%86%B5" target="_blank"> &gt;&gt; </el-link></div>
            <div>文件夹解析支持：蓝奏云、蓝奏云优享、小飞机盘、123云盘</div>
          </div>
        </div>
        <div class="typo">
          <p>节点1: 回源请求数:{{ node1Info.parserTotal }}, 缓存请求数:{{ node1Info.cacheTotal }}, 总数:{{ node1Info.total }}</p>
        </div>
        
        <hr>
        
        <div class="main" v-loading="isLoading">
          <div class="grid-content">
            <!-- 开关按钮，控制是否自动读取剪切板 -->
            <el-switch v-model="autoReadClipboard" active-text="自动识别剪切板"></el-switch>

            <el-input placeholder="请粘贴分享链接(http://或https://)" v-model="link" id="url">
              <template #prepend>分享链接</template>
              <template #append v-if="!autoReadClipboard">
                <el-button @click="getPaste(true)">读取剪切板</el-button>
              </template>
            </el-input>
            
            <el-input placeholder="请输入密码" v-model="password" id="url">
              <template #prepend>分享密码</template>
            </el-input>
            
            <el-input v-show="directLink" :value="directLink" id="url">
              <template #prepend>智能直链</template>
              <template #append>
                <el-button v-clipboard:copy="directLink" v-clipboard:success="onCopy" v-clipboard:error="onError">
                  <el-icon><CopyDocument /></el-icon>
                </el-button>
              </template>
            </el-input>

            <p style="text-align: center">
              <el-button style="margin-left: 40px" @click="parseFile">解析文件</el-button>
              <el-button style="margin-left: 20px" @click="parseDirectory">解析目录</el-button>
              <el-button style="margin-left: 20px" @click="generateMarkdown">生成Markdown</el-button>
              <el-button style="margin-left: 20px" @click="generateQRCode">扫码下载</el-button>
              <el-button style="margin-left: 20px" @click="getStatistics">分享统计</el-button>
              <el-button style="margin-left: 20px" @click="goToClientLinks" type="primary">生成命令行链接</el-button>
            </p>
          </div>

          <!-- 解析结果 -->
          <div v-if="parseResult.code" style="margin-top: 10px">
            <strong>解析结果: </strong>
            <json-viewer :value="parseResult" :expand-depth="5" copyable boxed sort />
            <!-- 文件信息美化展示区 -->
            <div v-if="downloadUrl" class="file-meta-info-card">
              <div class="file-meta-row">
                <span class="file-meta-label">下载链接：</span>
                <a :href="downloadUrl" target="_blank" class="file-meta-link" rel="noreferrer noopener">点击下载</a>
              </div>
              <div class="file-meta-row" v-if="parseResult.data?.downloadShortUrl">
                <span class="file-meta-label">下载短链：</span>
                <a :href="parseResult.data.downloadShortUrl" target="_blank" class="file-meta-link">{{ parseResult.data.downloadShortUrl }}</a>
              </div>
              <div class="file-meta-row">
                <span class="file-meta-label">文件预览：</span>
                <a :href="getPreviewLink()" target="_blank" class="file-meta-link">点击预览</a>
              </div>
              <div class="file-meta-row">
                <span class="file-meta-label">文件名：</span>{{ extractFileNameAndExt(downloadUrl).name }}
              </div>
              <div class="file-meta-row">
                <span class="file-meta-label">文件类型：</span>{{ getFileTypeClass({ fileName: extractFileNameAndExt(downloadUrl).name }) }}
              </div>
              <div class="file-meta-row" v-if="parseResult.data?.sizeStr">
                <span class="file-meta-label">文件大小：</span>{{ parseResult.data.sizeStr }}
              </div>
            </div>
          </div>

          <!-- Markdown链接 -->
          <div v-if="markdownText" style="text-align: center">
            <el-input :value="markdownText" readonly>
              <template #append>
                <el-button v-clipboard:copy="markdownText" v-clipboard:success="onCopy" v-clipboard:error="onError">
                  <el-icon><CopyDocument /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>

          <!-- 二维码 -->
          <div style="text-align: center" v-show="showQRCode">
            <canvas ref="qrcodeCanvas"></canvas>
            <div style="text-align: center">
              <el-link target="_blank" :href="qrCodeUrl">{{ qrCodeUrl }}</el-link>
            </div>
          </div>

          <!-- 统计信息 -->
          <div v-if="statisticsData.shareLinkInfo">
            <el-descriptions class="margin-top" title="分享详情" :column="1" border>
              <template slot="extra">
                <el-button type="primary" size="small">操作</el-button>
              </template>
              <el-descriptions-item label="网盘名称">{{ statisticsData.shareLinkInfo.panName }}</el-descriptions-item>
              <el-descriptions-item label="网盘标识">{{ statisticsData.shareLinkInfo.type }}</el-descriptions-item>
              <el-descriptions-item label="分享Key">{{ statisticsData.shareLinkInfo.shareKey }}</el-descriptions-item>
              <el-descriptions-item label="分享链接">
                <el-link target="_blank" :href="statisticsData.shareLinkInfo.shareUrl">{{ statisticsData.shareLinkInfo.shareUrl }}</el-link>
              </el-descriptions-item>
              <el-descriptions-item label="jsonApi链接">
                <el-link target="_blank" :href="statisticsData.apiLink">{{ statisticsData.apiLink }}</el-link>
              </el-descriptions-item>
              <el-descriptions-item label="302下载链接">
                <el-link target="_blank" :href="statisticsData.downLink">{{ statisticsData.downLink }}</el-link>
              </el-descriptions-item>
              <el-descriptions-item label="302预览链接">
                <el-link target="_blank" :href="statisticsData.viewLink">{{ statisticsData.viewLink }}</el-link>
              </el-descriptions-item>
              <el-descriptions-item label="解析次数">{{ statisticsData.parserTotal }}</el-descriptions-item>
              <el-descriptions-item label="缓存命中次数">{{ statisticsData.cacheHitTotal }}</el-descriptions-item>
              <el-descriptions-item label="总请求次数">{{ statisticsData.sumTotal }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 错误时显示小按钮 -->
          <div v-if="errorButtonVisible" style="text-align: center; margin-top: 10px;">
            <el-button type="text" @click="errorDialogVisible = true"> 反馈错误详情>> </el-button>
          </div>

          <!-- 错误 JSON 弹窗 -->
          <el-dialog
              v-model="errorDialogVisible"
              width="60%">
            <template #title>
              错误详情
              <el-link
                  @click.prevent="copyErrorDetails"
                  target="_blank"
                  style="margin-left:8px"
                  type="primary">
                复制当前错误信息，提交Issue
              </el-link>
            </template>
            <json-viewer :value="errorDetail" :expand-depth="5" copyable boxed sort />
            <template #footer>
              <el-button @click="errorDialogVisible = false">关闭</el-button>
            </template>
          </el-dialog>

          <!-- 临时认证配置弹窗 -->
          <el-dialog
              v-model="showAuthConfigDialog"
              title="临时认证配置"
              width="550px"
              :close-on-click-modal="false">
            <el-form :model="authConfig" label-width="100px" size="default">
              <el-form-item label="网盘类型" required>
                <el-select v-model="authConfig.panType" placeholder="请选择网盘类型" style="width: 100%" @change="onPanTypeChange">
                  <el-option-group label="必须认证">
                    <el-option label="夸克网盘 (QK)" value="QK">
                      <span>夸克网盘 (QK)</span>
                      <el-tag size="small" type="danger" style="margin-left: 8px">必须</el-tag>
                    </el-option>
                    <el-option label="UC网盘 (UC)" value="UC">
                      <span>UC网盘 (UC)</span>
                      <el-tag size="small" type="danger" style="margin-left: 8px">必须</el-tag>
                    </el-option>
                  </el-option-group>
                  <el-option-group label="大文件需认证">
                    <el-option label="小飞机网盘 (FJ)" value="FJ">
                      <span>小飞机网盘 (FJ)</span>
                      <el-tag size="small" type="warning" style="margin-left: 8px">大文件</el-tag>
                    </el-option>
                    <el-option label="蓝奏优享 (IZ)" value="IZ">
                      <span>蓝奏优享 (IZ)</span>
                      <el-tag size="small" type="warning" style="margin-left: 8px">大文件</el-tag>
                    </el-option>
                  </el-option-group>
                </el-select>
              </el-form-item>
              
              <el-form-item label="认证类型">
                <el-select v-model="authConfig.authType" placeholder="请选择认证类型" style="width: 100%">
                  <el-option 
                    v-for="opt in getSupportedAuthTypes()" 
                    :key="opt.value" 
                    :label="opt.label" 
                    :value="opt.value" />
                </el-select>
              </el-form-item>
              
              <el-form-item v-if="authConfig.authType === 'password'" label="用户名">
                <el-input v-model="authConfig.username" placeholder="请输入用户名" />
              </el-form-item>
              
              <el-form-item v-if="authConfig.authType === 'password'" label="密码">
                <el-input v-model="authConfig.password" type="password" show-password placeholder="请输入密码" />
              </el-form-item>
              
              <el-form-item v-if="authConfig.authType && authConfig.authType !== 'password'" label="Token/Cookie">
                <el-input 
                  v-model="authConfig.token" 
                  type="textarea" 
                  :rows="3" 
                  :placeholder="getTokenPlaceholder()" />
              </el-form-item>
              
              <el-form-item v-if="authConfig.authType === 'custom'" label="扩展字段1">
                <el-input v-model="authConfig.ext1" placeholder="格式: key:value" />
              </el-form-item>
              
              <el-form-item v-if="authConfig.authType === 'custom'" label="扩展字段2">
                <el-input v-model="authConfig.ext2" placeholder="格式: key:value" />
              </el-form-item>
              
              <el-alert 
                :type="getPanAuthAlertType()"
                :closable="false"
                show-icon
                style="margin-bottom: 15px;">
                <template #title>
                  <span>{{ getPanAuthHint() }}</span>
                </template>
              </el-alert>
              
              <!-- 已配置的网盘列表 -->
              <div v-if="Object.keys(allAuthConfigs).length > 0" style="margin-top: 10px;">
                <el-divider content-position="left">已配置的网盘</el-divider>
                <el-tag 
                  v-for="(config, panType) in allAuthConfigs" 
                  :key="panType"
                  closable
                  :type="panType === authConfig.panType ? 'primary' : 'info'"
                  style="margin-right: 8px; margin-bottom: 8px; cursor: pointer;"
                  @click="loadPanConfig(panType)"
                  @close="removePanConfig(panType)">
                  {{ getPanDisplayName(panType) }}
                </el-tag>
              </div>
            </el-form>
            
            <template #footer>
              <el-button @click="clearAuthConfig">
                <el-icon><Delete /></el-icon> 清除配置
              </el-button>
              <el-button @click="showAuthConfigDialog = false">取消</el-button>
              <el-button type="primary" @click="saveAuthConfig">
                <el-icon><Check /></el-icon> 保存
              </el-button>
            </template>
          </el-dialog>

          <!-- 目录树组件 -->
          <div v-if="showDirectoryTree" class="directory-tree-container">
            <div style="margin-bottom: 10px; text-align: right;">
              <el-radio-group v-model="directoryViewMode" size="small">
                <el-radio-button label="pane">窗格</el-radio-button>
                <el-radio-button label="tree">文件树</el-radio-button>
              </el-radio-group>
            </div>
            <DirectoryTree 
              :file-list="directoryData" 
              :share-url="link"
              :password="password"
              :view-mode="directoryViewMode"
              @file-click="handleFileClick"
            />
          </div>
        </div>
      </el-card>
    </el-row>
    
    <!-- 版本号显示 -->
    <div class="version-info">
      <span class="version-text">内部版本: {{ buildVersion }}</span>
      <el-link v-if="playgroundEnabled" :href="'/playground'" class="playground-link">脚本演练场</el-link>
    </div>
    
    <!-- 文件解析结果区下方加分享按钮 -->
<!--    <div v-if="parseResult.code && downloadUrl" style="margin-top: 10px; text-align: right;">-->
<!--      <el-button type="primary" @click="copyShowFileLink">分享文件直链</el-button>-->
<!--    </div>-->
    <!-- 目录解析结果区下方加分享按钮 -->
<!--    <div v-if="showDirectoryTree && directoryData.length" style="margin-top: 10px; text-align: right;">-->
<!--      <el-input :value="showListLink" readonly style="width: 350px; margin-right: 10px;">-->
<!--        <template #append>-->
<!--          <el-button v-clipboard:copy="showListLink" v-clipboard:success="onCopy" v-clipboard:error="onError">-->
<!--            <el-icon><CopyDocument /></el-icon>复制分享链接-->
<!--          </el-button>-->
<!--        </template>-->
<!--      </el-input>-->
<!--    </div>-->

    <!-- 捐赠账号弹窗 -->
    <el-dialog
        v-model="showDonateDialog"
        title="🎁 捐赠网盘账号"
        width="550px"
        :close-on-click-modal="false">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 15px;">
        <template #title>
          捐赠您的网盘 Cookie/Token，解析时将从所有捐赠账号中随机选择使用，分摊请求压力。
        </template>
      </el-alert>
      
      <!-- 已捐赠账号数量统计 -->
      <div v-if="donateAccountCounts.total > 0" style="margin-bottom: 16px;">
        <el-divider content-position="left">当前账号池（共 {{ donateAccountCounts.total }} 个）</el-divider>
        <el-tag 
          v-for="(count, panType) in donateAccountCounts" 
          :key="panType"
          v-show="panType !== 'total'"
          type="success"
          style="margin-right: 6px; margin-bottom: 4px;">
          {{ getPanDisplayName(panType) }}: {{ count }} 个
        </el-tag>
      </div>
      <div v-else style="margin-bottom: 16px; text-align: center; color: #999;">
        暂无捐赠账号，成为第一个捐赠者吧！
      </div>
      
      <el-form :model="donateConfig" label-width="100px" size="default">
        <el-form-item label="网盘类型" required>
          <el-select v-model="donateConfig.panType" placeholder="请选择网盘类型" style="width: 100%" @change="onDonatePanTypeChange">
            <el-option-group label="必须认证">
              <el-option label="夸克网盘 (QK)" value="QK" />
              <el-option label="UC网盘 (UC)" value="UC" />
            </el-option-group>
            <el-option-group label="大文件需认证">
              <el-option label="小飞机网盘 (FJ)" value="FJ" />
              <el-option label="蓝奏优享 (IZ)" value="IZ" />
              <el-option label="123云盘 (YE)" value="YE" />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="认证类型">
          <el-select v-model="donateConfig.authType" placeholder="请选择认证类型" style="width: 100%">
            <el-option 
              v-for="opt in getDonateAuthTypes()" 
              :key="opt.value" 
              :label="opt.label" 
              :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="donateConfig.authType === 'password'" label="用户名">
          <el-input v-model="donateConfig.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="donateConfig.authType === 'password'" label="密码">
          <el-input v-model="donateConfig.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item v-if="donateConfig.authType && donateConfig.authType !== 'password'" label="Token/Cookie">
          <el-input 
            v-model="donateConfig.token" 
            type="textarea" 
            :rows="3" 
            placeholder="粘贴 Cookie 或 Token（从浏览器开发者工具获取）" />
        </el-form-item>
        <el-form-item label="备注（可选）">
          <el-input v-model="donateConfig.remark" placeholder="如：我的夸克小号" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showDonateDialog = false">关闭</el-button>
        <el-button type="primary" @click="submitDonateAccount" :loading="donateSubmitting">
          <el-icon><Plus /></el-icon> 捐赠此账号
        </el-button>
      </template>
    </el-dialog>

  </div>

</template>

<script>
import axios from 'axios'
import QRCode from 'qrcode'
import DarkMode from '@/components/DarkMode'
import DirectoryTree from '@/components/DirectoryTree'
import parserUrl from '../parserUrl1'
import fileTypeUtils from '@/utils/fileTypeUtils'
import { ElMessage } from 'element-plus'
import { playgroundApi } from '@/utils/playgroundApi'

export const previewBaseUrl = 'https://nfd-parser.github.io/nfd-preview/preview.html?src=';

export default {
  name: 'App',
  components: { DarkMode, DirectoryTree },
  mixins: [fileTypeUtils],
  data() {
    return {
      baseAPI: `${location.protocol}//${location.host}`,
      autoReadClipboard: true,
      isDarkMode: false,
      isLoading: false,
      
      // 输入数据
      link: "",
      password: "",
      
      // 解析结果
      parseResult: {},
      downloadUrl: null,
      directLink: '',
      previewBaseUrl,
      
      // 功能结果
      markdownText: '',
      showQRCode: false,
      qrCodeUrl: '',
      statisticsData: {},
      
      // 目录树
      showDirectoryTree: false,
      directoryData: [],
      
      // 统计信息
      node1Info: {},
      node2Info: {},
      hasWarnedNoLink: false,
      directoryViewMode: 'pane', // 新增，目录树展示模式
      hasClipboardSuccessTip: false, // 新增，聚焦期间只提示一次
      showRiskDialog: false,
      baseUrl: location.origin,
      showListLink: '',

      errorDialogVisible: false,
      errorDetail: null,
      errorButtonVisible: false,
      
      // 版本信息
      buildVersion: '',
      
      // 演练场启用状态
      playgroundEnabled: false,
      
      // 临时认证配置
      showAuthConfigDialog: false,
      authConfig: {
        panType: '',     // 网盘类型: QK, UC, FJ, IZ
        authType: 'cookie',
        username: '',
        password: '',
        token: '',
        cookie: '',
        auth: '',
        ext1: '',
        ext2: '',
        ext3: '',
        ext4: '',
        ext5: ''
      },
      // 所有网盘的认证配置 { panType: config }
      allAuthConfigs: {},
      
      // 捐赠账号相关
      showDonateDialog: false,
      donateSubmitting: false,
      donateConfig: {
        panType: '',
        authType: 'cookie',
        username: '',
        password: '',
        token: '',
        remark: ''
      },
      // 捐赠账号数量统计 { panType: count, total: N }
      donateAccountCounts: { total: 0 }
    }
  },
  computed: {
    // 检查是否配置了认证信息（针对当前链接的网盘类型）
    hasAuthConfig() {
      const panType = this.getCurrentPanType()
      if (!panType) return false
      return !!this.allAuthConfigs[panType]
    },
    // 获取已配置认证的网盘数量
    authConfigCount() {
      return Object.keys(this.allAuthConfigs).length
    }
  },
  methods: {
    // 从分享链接中提取网盘类型
    getCurrentPanType() {
      if (!this.link) return ''
      const url = this.link.toLowerCase()
      if (url.includes('quark.cn') || url.includes('pan.quark.cn')) return 'QK'
      if (url.includes('drive.uc.cn') || url.includes('fast.uc.cn')) return 'UC'
      if (url.includes('feijipan.com') || url.includes('feijihe.com') || url.includes('xiaofeiyang.com')) return 'FJ'
      if (url.includes('ilanzou.com') || url.includes('lanzouv.com')) return 'IZ'
      if (url.includes('123pan.com') || url.includes('123684.com') || url.includes('123865.com')) return 'YE'
      return ''
    },
    
    // 获取网盘显示名称
    getPanDisplayName(panType) {
      const names = {
        'QK': '夸克网盘',
        'UC': 'UC网盘',
        'FJ': '小飞机网盘',
        'IZ': '蓝奏优享',
        'YE': '123云盘'
      }
      return names[panType] || panType
    },
    
    // 获取认证提示信息
    getPanAuthHint() {
      const hints = {
        'QK': '夸克网盘必须配置 Cookie 才能解析和下载（登录后从浏览器开发者工具获取）',
        'UC': 'UC网盘必须配置 Cookie 才能解析和下载（登录后从浏览器开发者工具获取）',
        'FJ': '小飞机网盘大文件（>100MB）需要配置认证信息',
        'IZ': '蓝奏优享大文件需要配置认证信息'
      }
      return hints[this.authConfig.panType] || '请选择网盘类型后配置认证信息'
    },
    
    // 获取提示类型
    getPanAuthAlertType() {
      if (!this.authConfig.panType) return 'info'
      if (this.authConfig.panType === 'QK' || this.authConfig.panType === 'UC') return 'warning'
      return 'info'
    },
    
    // 根据网盘类型获取支持的认证方式列表
    getSupportedAuthTypes() {
      const panType = this.authConfig.panType?.toLowerCase() || ''
      
      // 定义所有认证类型
      const allAuthTypes = {
        cookie: { label: 'Cookie', value: 'cookie' },
        accesstoken: { label: 'AccessToken', value: 'accesstoken' },
        authorization: { label: 'Authorization', value: 'authorization' },
        password: { label: '用户名密码', value: 'password' },
        custom: { label: '自定义', value: 'custom' }
      }
      
      // 根据网盘类型返回支持的认证方式
      switch (panType) {
        case 'qk':  // 夸克网盘：只支持Cookie
        case 'uc':  // UC网盘：只支持Cookie
        case 'qqwy': // QQ微云：只支持Cookie
        case 'pali': // 阿里云盘：只支持Cookie
          return [allAuthTypes.cookie]
          
        case 'fj':  // 小飞机网盘：只支持用户名密码
        case 'iz':  // 蓝奏优享：只支持用户名密码
          return [allAuthTypes.password]
          
        case 'ye':  // 123网盘：支持用户名密码和Authorization
          return [allAuthTypes.password, allAuthTypes.authorization]
          
        case 'p189': // 天翼云盘：支持用户名密码、AccessToken、Cookie
          return [allAuthTypes.password, allAuthTypes.accesstoken, allAuthTypes.cookie]
          
        case 'p139': // 移动云盘：支持Authorization
          return [allAuthTypes.authorization]
          
        case 'pwo':  // 联通云盘：支持AccessToken
          return [allAuthTypes.accesstoken]
          
        default:
          // 默认显示所有选项
          return Object.values(allAuthTypes)
      }
    },
    
    // 网盘类型变更时加载对应配置
    onPanTypeChange(panType) {
      // 先临时设置panType以便获取支持的认证类型
      const tempAuthConfig = { ...this.authConfig, panType }
      this.authConfig = tempAuthConfig
      
      // 获取该网盘支持的认证类型
      const supportedTypes = this.getSupportedAuthTypes()
      const defaultAuthType = supportedTypes.length > 0 ? supportedTypes[0].value : 'cookie'
      
      if (this.allAuthConfigs[panType]) {
        // 加载已有配置
        const config = this.allAuthConfigs[panType]
        this.authConfig = { ...this.authConfig, ...config, panType }
        // 确保认证类型在支持列表中
        if (!supportedTypes.find(t => t.value === this.authConfig.authType)) {
          this.authConfig.authType = defaultAuthType
        }
      } else {
        // 重置为默认值，使用该网盘默认的认证类型
        this.authConfig = {
          panType,
          authType: defaultAuthType,
          username: '',
          password: '',
          token: '',
          cookie: '',
          auth: '',
          ext1: '',
          ext2: '',
          ext3: '',
          ext4: '',
          ext5: ''
        }
      }
    },
    
    // 加载指定网盘的配置
    loadPanConfig(panType) {
      this.authConfig.panType = panType
      this.onPanTypeChange(panType)
    },
    
    // 删除指定网盘的配置
    removePanConfig(panType) {
      delete this.allAuthConfigs[panType]
      localStorage.setItem('nfd_auth_configs', JSON.stringify(this.allAuthConfigs))
      if (this.authConfig.panType === panType) {
        this.authConfig.panType = ''
      }
      this.$message.success(`已删除 ${this.getPanDisplayName(panType)} 的认证配置`)
      this.updateDirectLink()
    },
    
    // 获取 Token 输入框的提示文本
    getTokenPlaceholder() {
      const placeholders = {
        'accesstoken': '请输入 AccessToken',
        'cookie': '请输入 Cookie，例如: __puus=xxx; __pus=xxx（从浏览器开发者工具获取）',
        'authorization': '请输入 Authorization 头内容，例如: Bearer xxx',
        'custom': '请输入主 Token'
      }
      return placeholders[this.authConfig.authType] || '请输入认证信息'
    },
    
    // 保存认证配置
    saveAuthConfig() {
      if (!this.authConfig.panType) {
        this.$message.warning('请先选择网盘类型')
        return
      }
      if (!this.authConfig.authType) {
        this.$message.warning('请选择认证类型')
        return
      }
      if (!this.authConfig.token && !this.authConfig.username) {
        this.$message.warning('请填写认证信息')
        return
      }
      
      // 保存到配置集合
      const configToSave = { ...this.authConfig }
      this.allAuthConfigs[this.authConfig.panType] = configToSave
      
      // 持久化到 localStorage
      localStorage.setItem('nfd_auth_configs', JSON.stringify(this.allAuthConfigs))
      this.showAuthConfigDialog = false
      this.$message.success(`${this.getPanDisplayName(this.authConfig.panType)} 认证配置已保存`)
      // 更新智能直链
      this.updateDirectLink()
    },
    
    // 清除所有认证配置
    clearAuthConfig() {
      this.authConfig = {
        panType: '',
        authType: 'cookie',
        username: '',
        password: '',
        token: '',
        cookie: '',
        auth: '',
        ext1: '',
        ext2: '',
        ext3: '',
        ext4: '',
        ext5: ''
      }
      this.allAuthConfigs = {}
      localStorage.removeItem('nfd_auth_configs')
      this.$message.success('所有认证配置已清除')
      this.showAuthConfigDialog = false
      // 更新智能直链
      this.updateDirectLink()
    },
    
    // 加载认证配置
    loadAuthConfig() {
      const saved = localStorage.getItem('nfd_auth_configs')
      if (saved) {
        try {
          this.allAuthConfigs = JSON.parse(saved)
        } catch (e) {
          console.error('加载认证配置失败:', e)
        }
      }
    },
    
    // 生成加密的 auth 参数（优先使用个人配置，否则从后端随机获取捐赠账号）
    async generateAuthParam() {
      const panType = this.getCurrentPanType()
      if (!panType) return ''
      
      let config = null
      
      // 优先使用个人配置
      if (this.allAuthConfigs[panType]) {
        config = this.allAuthConfigs[panType]
        console.log(`[认证] 使用个人配置: ${this.getPanDisplayName(panType)}`)
      } else {
        // 从后端随机获取捐赠账号
        try {
          const response = await axios.get(`${this.baseAPI}/v2/randomAuth`, { params: { panType } })
          // 解包 JsonResult 嵌套
          let data = response.data
          while (data && data.data !== undefined && data.code !== undefined) {
            data = data.data
          }
          if (data && (data.token || data.username)) {
            config = data
            console.log(`[认证] 使用捐赠账号: ${this.getPanDisplayName(panType)}`)
          }
        } catch (e) {
          console.log(`[认证] 无可用捐赠账号: ${this.getPanDisplayName(panType)}`)
        }
      }
      
      if (!config) return ''
      
      // 构建 JSON 对象
      const authObj = {}
      if (config.authType) authObj.authType = config.authType
      if (config.username) authObj.username = config.username
      if (config.password) authObj.password = config.password
      if (config.token) authObj.token = config.token
      if (config.cookie) authObj.cookie = config.cookie
      if (config.auth) authObj.auth = config.auth
      if (config.ext1) authObj.ext1 = config.ext1
      if (config.ext2) authObj.ext2 = config.ext2
      if (config.ext3) authObj.ext3 = config.ext3
      if (config.ext4) authObj.ext4 = config.ext4
      if (config.ext5) authObj.ext5 = config.ext5
      
      // AES 加密 + Base64 + URL 编码
      try {
        const jsonStr = JSON.stringify(authObj)
        const encrypted = this.aesEncrypt(jsonStr, 'nfd_auth_key2026')
        return encodeURIComponent(encrypted)
      } catch (e) {
        console.error('生成认证参数失败:', e)
        return ''
      }
    },
    
    // AES 加密 (ECB 模式, PKCS5Padding)
    aesEncrypt(text, key) {
      // 使用 CryptoJS 进行 AES 加密
      const CryptoJS = require('crypto-js')
      const keyBytes = CryptoJS.enc.Utf8.parse(key)
      const encrypted = CryptoJS.AES.encrypt(text, keyBytes, {
        mode: CryptoJS.mode.ECB,
        padding: CryptoJS.pad.Pkcs7
      })
      return encrypted.toString() // Base64 编码
    },
    
    // 更新智能直链
    async updateDirectLink() {
      if (this.link) {
        const authParam = await this.generateAuthParam()
        const authSuffix = authParam ? `&auth=${authParam}` : ''
        this.directLink = `${this.baseAPI}/parser?url=${this.link}${this.password ? `&pwd=${this.password}` : ''}${authSuffix}`
      }
    },

    // 生成预览链接（WPS 云文档特殊处理）
    getPreviewLink() {
      // 判断 shareKey 是否以 pwps: 开头（WPS 云文档）
      const shareKey = this.parseResult?.data?.shareKey
      if (shareKey && shareKey.startsWith('pwps:')) {
        // WPS 云文档直接使用原始分享链接
        return this.link
      }
      // 其他类型使用默认预览服务
      return this.previewBaseUrl + encodeURIComponent(this.downloadUrl)
    },
    
    // 主题切换
    handleThemeChange(isDark) {
      this.isDarkMode = isDark
      if (document.body && document.body.classList) {
        document.body.classList.toggle('dark-theme', isDark)
      }
      window.localStorage.setItem('isDarkMode', isDark)

    },

    // 验证输入
    validateInput() {
      this.clearResults()
      
      if (!this.link.startsWith("https://") && !this.link.startsWith("http://")) {
        this.$message.error("请输入有效链接!")
        throw new Error('请输入有效链接')
      }
    },

    // 清除结果
    clearResults() {
      this.parseResult = {}
      this.downloadUrl = null
      this.markdownText = ''
      this.showQRCode = false
      this.statisticsData = {}
      this.showDirectoryTree = false
      this.directoryData = []
    },

    // 统一API调用（自动添加认证参数）
    async callAPI(endpoint, params = {}) {
      this.errorButtonVisible = false
      try {
        this.isLoading = true
        // 添加认证参数（异步获取）
        const authParam = await this.generateAuthParam()
        if (authParam) {
          params.auth = authParam
        }
        const response = await axios.get(`${this.baseAPI}${endpoint}`, { params })
        
        if (response.data.code === 200) {
          // this.$message.success(response.data.msg || '操作成功')
          return response.data
        } else {
          // 在页面右下角显示一个“查看详情”按钮 可以查看原json
          this.errorDetail = response?.data
          this.errorButtonVisible = true
          throw new Error(response.data.msg || '操作失败')
        }
      } catch (error) {
        this.$message.error(error.message || '网络错误')
        throw error
      } finally {
        this.isLoading = false
      }
    },

    // 文件解析
    async parseFile() {
      try {
        this.validateInput()
        
        // 先调用 linkInfo 获取网盘类型
        const linkInfoResult = await this.callAPI('/v2/linkInfo', { 
          url: this.link,
          ...(this.password && { pwd: this.password })
        })
        
        const panType = linkInfoResult.data?.shareLinkInfo?.type
        const panName = linkInfoResult.data?.shareLinkInfo?.panName || '未知网盘'
        
        // 根据网盘类型给出提示
        if (panType === 'qk' || panType === 'uc') {
          // UC和夸克：提示使用命令行下载
          this.$message.warning({
            message: `${panName}无法在网页端直接下载，请点击"生成下载命令"按钮，使用命令行工具下载`,
            duration: 5000,
            showClose: true
          })
        } else if (panType === 'fj' || panType === 'lz' || panType === 'iz' || panType === 'le') {
          // 小飞机、蓝奏、优享、联想乐云：提示大文件需要认证
          const hasAuth = this.allAuthConfigs[panType]?.cookie || 
                          this.allAuthConfigs[panType]?.username
          if (!hasAuth) {
            this.$message.info({
              message: `${panName}的大文件解析需要配置认证信息，请在"配置认证"中添加`,
              duration: 4000,
              showClose: true
            })
          }
        }
        
        // 继续解析文件
        const params = { url: this.link }
        if (this.password) params.pwd = this.password
        
        const result = await this.callAPI('/json/parser', params)
        this.parseResult = result
        this.downloadUrl = result.data?.directLink
        // 更新智能直链（包含认证参数）
        this.updateDirectLink()
        this.$message.success('文件解析成功！')
      } catch (error) {
        console.error('文件解析失败:', error)
      }
    },

    // 目录解析
    async parseDirectory() {
      try {
        this.validateInput()
        const params = { url: this.link }
        if (this.password) params.pwd = this.password
        
        const result = await this.callAPI('/v2/linkInfo', params)
        const data = result.data
        
        // 检查是否支持目录解析
        const supportedPans = ["iz", "lz", "fj", "ye", "le"]
        if (!supportedPans.includes(data.shareLinkInfo.type)) {
          this.$message.error("当前网盘不支持目录解析")
          return
        }

        // 获取目录数据
        const directoryResult = await this.callAPI('/v2/getFileList', params)
        this.directoryData = directoryResult.data || []
        this.showDirectoryTree = true
        // 自动赋值分享链接
        this.showListLink = `${this.baseUrl}/showList?url=${encodeURIComponent(this.link)}`

        this.$message.success(`目录解析成功！共找到 ${this.directoryData.length} 个文件/文件夹`)
      } catch (error) {
        console.error('目录解析失败:', error)
      }
    },

    // 生成Markdown
    async generateMarkdown() {
      try {
        this.validateInput()
        const params = { url: this.link }
        if (this.password) params.pwd = this.password
        
        const result = await this.callAPI('/v2/linkInfo', params)
        this.markdownText = this.buildMarkdown('快速下载地址', result.data.downLink)
        this.$message.success('Markdown生成成功！')
      } catch (error) {
        console.error('生成Markdown失败:', error)
      }
    },

    // 生成二维码
    async generateQRCode() {
      try {
        this.validateInput()
        const params = { url: this.link }
        if (this.password) params.pwd = this.password
        
        const result = await this.callAPI('/v2/linkInfo', params)
        this.qrCodeUrl = result.data.downLink
        
        const options = {
          width: 150,
          height: 150,
          margin: 2
        }
        
        QRCode.toCanvas(this.$refs.qrcodeCanvas, this.qrCodeUrl, options, error => {
          if (error) console.error(error)
        })
        
        this.showQRCode = true
        this.$message.success('二维码生成成功！')
      } catch (error) {
        console.error('生成二维码失败:', error)
      }
    },

    // 获取统计信息
    async getStatistics() {
      try {
        this.validateInput()
        const params = { url: this.link }
        if (this.password) params.pwd = this.password
        
        const result = await this.callAPI('/v2/linkInfo', params)
        this.statisticsData = result.data
        this.$message.success('统计信息获取成功！')
      } catch (error) {
        console.error('获取统计信息失败:', error)
      }
    },

    // 构建Markdown链接
    buildMarkdown(title, url) {
      return `[${title}](${url})`
    },

    // 复制成功
    onCopy() {
      this.$message.success('复制成功')
    },

    // 复制失败
    onError() {
      this.$message.error('复制失败')
    },

    // 文件点击处理
    handleFileClick(file) {
      if (file.parserUrl) {
        window.open(file.parserUrl, '_blank')
      } else {
        this.$message.warning('该文件暂无下载链接')
      }
    },

    // 获取剪切板内容
    async getPaste(isManual = false) {
      try {
        const text = await navigator.clipboard.readText()
        console.log('获取到的文本内容是：', text)
        
        const linkInfo = parserUrl.parseLink(text)
        const pwd = parserUrl.parsePwd(text) || ''
        
        if (linkInfo.link) {
          if (linkInfo.link !== this.link || pwd !== this.password) {
            this.password = pwd
            this.link = linkInfo.link
            // 更新智能直链（包含认证参数）
            this.updateDirectLink()
            // 聚焦期间只提示一次
            if (!this.hasClipboardSuccessTip) {
              this.$message.success(`自动识别分享成功, 网盘类型: ${linkInfo.name}; 分享URL ${this.link}; 分享密码: ${this.password || '空'}`)
              this.hasClipboardSuccessTip = true
            }
          } else {
            this.$message.warning(`[${linkInfo.name}]分享信息无变化`)
          }
          this.hasWarnedNoLink = false // 有效链接后重置
        } else {
          if (isManual || !this.hasWarnedNoLink) {
            this.$message.warning("未能提取到分享链接, 该分享可能尚未支持, 你可以复制任意网盘/音乐App的分享到该页面, 系统智能识别")
            this.hasWarnedNoLink = true
          }
        }
      } catch (error) {
        console.error('读取剪切板失败:', error)
        this.$message.error('读取剪切板失败，请检查浏览器权限')
      }
    },

    // 获取统计信息
    async getInfo() {
      try {
        const response = await axios.get('/v2/statisticsInfo')
        if (response.data.success) {
          this.node1Info = response.data.data
        }
      } catch (error) {
        console.error('获取统计信息失败:', error)
      }
    },

    // 获取版本号
    async getBuildVersion() {
      try {
        const response = await axios.get('/v2/build-version')
        this.buildVersion = response.data.data
      } catch (error) {
        console.error('获取版本号失败:', error)
        this.buildVersion = 'unknown'
      }
    },

    // 检查演练场是否启用
    async checkPlaygroundEnabled() {
      try {
        const result = await playgroundApi.getStatus()
        if (result && result.data) {
          this.playgroundEnabled = result.data.enabled === true
        }
      } catch (error) {
        console.error('检查演练场状态失败:', error)
        this.playgroundEnabled = false
      }
    },

    // 新增切换目录树展示模式方法
    setDirectoryViewMode(mode) {
      this.directoryViewMode = mode
    },

    // 文件名和类型提取方法（复用 DirectoryTree 的静态方法）
    extractFileNameAndExt(url) {
      return fileTypeUtils.extractFileNameAndExt(url)
    },
    getFileTypeClass(file) {
      return fileTypeUtils.getFileTypeClass(file)
    },
    ackRisk() {
      window.localStorage.setItem('nfd_risk_ack', '1')
      this.showRiskDialog = false
    },
    copyShowFileLink() {
      const url = `${this.baseUrl}/showFile?url=${encodeURIComponent(this.downloadUrl)}`
      navigator.clipboard.writeText(url).then(() => {
        ElMessage.success('文件分享链接已复制！')
      })
    },
    copyShowListLink() {
      const url = `${this.baseUrl}/showList?url=${encodeURIComponent(this.link)}`
      navigator.clipboard.writeText(url).then(() => {
        ElMessage.success('目录分享链接已复制！')
      })
    },

    copyErrorDetails() {
      const text = `分享链接：${this.link}
分享密码：${this.password || ''}
错误信息：${JSON.stringify(this.errorDetail, null, 2)}`;
      navigator.clipboard.writeText(text).then(() => {
        this.$message.success('已复制分享信息和错误详情');
        window.open('https://github.com/qaiu/netdisk-fast-download/issues/new', '_blank');
      }).catch(() => {
        this.$message.error('复制失败');
      });
    },
    
    // 跳转到客户端链接页面
    async goToClientLinks() {
      // 验证输入
      if (!this.link.trim()) {
        this.$message.warning('请先输入分享链接')
        return
      }

      if (!this.link.startsWith("https://") && !this.link.startsWith("http://")) {
        this.$message.error("请输入有效链接!")
        return
      }

      try {
        // 显示加载状态
        this.isLoading = true
        
        // 直接使用 axios 请求客户端链接 API，因为它的响应格式与其他 API 不同
        const params = { url: this.link }
        if (this.password) params.pwd = this.password
        
        // 添加认证参数
        const authParam = await this.generateAuthParam()
        if (authParam) params.auth = authParam
        
        const response = await axios.get(`${this.baseAPI}/v2/clientLinks`, { params })
        const result = response.data
        
        // 处理包装格式的响应
        const clientData = result.data || result
        
        if (clientData.success) {
          // 将数据存储到 sessionStorage，供客户端链接页面使用
          sessionStorage.setItem('clientLinksData', JSON.stringify(clientData))
          sessionStorage.setItem('clientLinksForm', JSON.stringify({
            shareUrl: this.link,
            password: this.password
          }))
          
          // 跳转到客户端链接页面
          this.$router.push('/clientLinks')
          this.$message.success('客户端链接生成成功，正在跳转...')
        } else {
          this.$message.error(clientData.error || '生成客户端链接失败')
        }
      } catch (error) {
        console.error('生成客户端链接失败:', error)
        this.$message.error('生成客户端链接失败')
      } finally {
        this.isLoading = false
      }
    },
    
    // ========== 捐赠账号相关方法 ==========
    
    // 捐赠弹窗中网盘类型变更
    onDonatePanTypeChange(panType) {
      const types = this.getDonateAuthTypes()
      this.donateConfig.authType = types.length > 0 ? types[0].value : 'cookie'
      this.donateConfig.username = ''
      this.donateConfig.password = ''
      this.donateConfig.token = ''
      this.donateConfig.remark = ''
    },
    
    // 获取捐赠弹窗支持的认证类型
    getDonateAuthTypes() {
      const pt = (this.donateConfig.panType || '').toLowerCase()
      const allTypes = {
        cookie: { label: 'Cookie', value: 'cookie' },
        accesstoken: { label: 'AccessToken', value: 'accesstoken' },
        authorization: { label: 'Authorization', value: 'authorization' },
        password: { label: '用户名密码', value: 'password' },
        custom: { label: '自定义', value: 'custom' }
      }
      switch (pt) {
        case 'qk': case 'uc': return [allTypes.cookie]
        case 'fj': case 'iz': return [allTypes.password]
        case 'ye': return [allTypes.password, allTypes.authorization]
        default: return Object.values(allTypes)
      }
    },
    
    // 提交捐赠账号（调用后端 API）
    async submitDonateAccount() {
      if (!this.donateConfig.panType) {
        this.$message.warning('请选择网盘类型')
        return
      }
      if (!this.donateConfig.token && !this.donateConfig.username) {
        this.$message.warning('请填写认证信息（Cookie/Token 或 用户名密码）')
        return
      }
      
      this.donateSubmitting = true
      try {
        const payload = {
          panType: this.donateConfig.panType,
          authType: this.donateConfig.authType,
          username: this.donateConfig.username || '',
          password: this.donateConfig.password || '',
          token: this.donateConfig.token || '',
          remark: this.donateConfig.remark || ''
        }
        await axios.post(`${this.baseAPI}/v2/donateAccount`, payload)
        this.$message.success(`已捐赠 ${this.getPanDisplayName(this.donateConfig.panType)} 账号，感谢您的贡献！`)
        
        // 重置表单
        this.donateConfig.username = ''
        this.donateConfig.password = ''
        this.donateConfig.token = ''
        this.donateConfig.remark = ''
        
        // 刷新计数
        await this.loadDonateAccountCounts()
      } catch (e) {
        console.error('捐赠账号失败:', e)
        this.$message.error('捐赠失败，请稍后重试')
      } finally {
        this.donateSubmitting = false
      }
    },
    
    // 从后端加载捐赠账号数量统计
    async loadDonateAccountCounts() {
      try {
        const response = await axios.get(`${this.baseAPI}/v2/donateAccountCounts`)
        // 解包可能的 JsonResult 嵌套: { code, data: { code, data: { QK: 3, total: 4 } } }
        let data = response.data
        while (data && data.data !== undefined && data.code !== undefined) {
          data = data.data
        }
        if (data && typeof data === 'object') {
          // 确保有 total 字段
          if (data.total === undefined) {
            let total = 0
            for (const [key, val] of Object.entries(data)) {
              if (typeof val === 'number') total += val
            }
            data.total = total
          }
          this.donateAccountCounts = data
        }
      } catch (e) {
        console.error('加载捐赠账号统计失败:', e)
      }
    }
  },
  
  mounted() {
    // 从localStorage读取设置
    const savedAutoRead = window.localStorage.getItem("autoReadClipboard")
    if (savedAutoRead !== null) {
      this.autoReadClipboard = savedAutoRead === 'true'
    }

    // 加载认证配置
    this.loadAuthConfig()

    // 加载捐赠账号统计
    this.loadDonateAccountCounts()

    // 获取初始统计信息
    this.getInfo()

    // 获取版本号
    this.getBuildVersion()

    // 检查演练场是否启用
    this.checkPlaygroundEnabled()

    // 自动读取剪切板
    if (this.autoReadClipboard) {
      this.getPaste()
    }

    // 监听窗口焦点事件
    window.addEventListener('focus', () => {
      if (this.autoReadClipboard) {
        this.hasClipboardSuccessTip = false // 聚焦时重置，只提示一次
        this.getPaste()
      }
    })

    // 首次打开页面弹出风险提示
    if (!window.localStorage.getItem('nfd_risk_ack')) {
      this.showRiskDialog = true
    }
  },
  
  watch: {
    downloadUrl(val) {
      if (!val) {
        this.$router.push('/')
      }
    },
    autoReadClipboard(val) {
      window.localStorage.setItem("autoReadClipboard", val)
    }
  }
}
</script>

<style>
[v-cloak] { display: none; }

body {
  background-color: #f5f7fa;
  color: #2c3e50;
  margin: 0;
  padding: 0;
}
body.dark-theme {
  background-color: #181818;
  color: #ffffff;
}

#app {
  /* 不设置 background-color */
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  margin: auto;
  padding: 1em;
  max-width: 900px;
}

#app.dark-theme {
  /* 不设置 background-color */
  color: #ffffff;
}

.box-card {
  flex: 1;
  margin-top: 4em !important;
  margin-bottom: 4em !important;
  opacity: 1 !important; /* 只要不透明 */
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.08);
  border: none;
}

#app.dark-theme .box-card {
  background: #232323 !important;
  color: #fff !important;
  box-shadow: 0 10px 30px rgba(0,0,0,0.3);
  border: none;
}

@media screen and (max-width: 700px) {
  #app {
    padding-left: 0 !important;
    padding-right: 0 !important;
    margin: 0 !important; /* 关键：去掉 auto 居中 */
    max-width: 100vw !important;
  }
  #app .box-card {
    margin: 1em 4px !important; /* 上下1em，左右4px */
    width: auto !important;
    max-width: 100vw !important;
    box-sizing: border-box;
  }
}

.grid-content {
  margin-top: 1em;
  border-radius: 4px;
  min-height: 50px;
}

.el-select .el-input {
  width: 130px;
}

.directory-tree-container {
  margin-top: 20px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fafafa;
}

#app.dark-theme .directory-tree-container {
  background-color: #2d2d2d;
  border-color: #404040;
}

.download h3 {
  margin-top: 2em;
}

.download button {
  margin-right: 0.5em;
  margin-left: 0.5em;
}

.typo {
  text-align: left;
}

.typo a {
  color: #0077ff;
}

#app.dark-theme .typo a {
  color: #4a9eff;
}

hr {
  height: 10px;
  margin-bottom: .8em;
  border: none;
  border-bottom: 1px solid rgba(0, 0, 0, .12);
}

#app.dark-theme hr {
  border-bottom-color: rgba(255, 255, 255, .12);
}

.feedback-bar {
  width: 100%;
  text-align: right;
  padding: 10px 10px 0 0;
}
.feedback-link {
  color: #888;
  font-weight: 500;
  font-size: 0.98rem;
  text-decoration: none;
  border: none;
  border-radius: 5px;
  padding: 2px 10px;
  background: transparent;
  transition: background 0.2s, color 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin-left: 6px;
}
.feedback-link:first-child { margin-left: 0; }
.feedback-link:hover {
  background: #f0f0f0;
  color: #333;
}
.dark-theme .feedback-link {
  background: transparent;
  color: #bbb;
  border: none;
}
.dark-theme .feedback-link:hover {
  background: #333;
  color: #fff;
}
.feedback-link.mini {
  font-size: 0.92rem;
  padding: 2px 8px;
  border-radius: 4px;
}
.feedback-icon {
  font-size: 1em;
  color: #888;
  margin-right: 2px;
}
.feedback-link:hover .feedback-icon {
  color: #333;
}
.feedback-link:nth-child(2) .feedback-icon { color: #333; }
.feedback-link:nth-child(3) .feedback-icon { color: #f39c12; }
.dark-theme .feedback-icon {
  color: #bbb;
}
.dark-theme .feedback-link:nth-child(2) .feedback-icon { color: #fff; }
.dark-theme .feedback-link:nth-child(3) .feedback-icon { color: #f7ca77; }
.feedback-link:nth-child(4) .feedback-icon { color: #409eff; }
.dark-theme .feedback-link:nth-child(4) .feedback-icon { color: #4a9eff; }

.project-intro {
  margin: 0 auto 18px auto;
  max-width: 700px;
  text-align: center;
  color: #888;
  font-size: 1.02rem;
}
.intro-title {
  font-size: 1.18rem;
  font-weight: bold;
  margin-bottom: 4px;
  color: #666;
}
.intro-desc {
  color: #888;
  font-size: 0.98rem;
  line-height: 1.7;
}
.dark-theme .project-intro, .dark-theme .intro-title, .dark-theme .intro-desc {
  color: #bbb;
}
.dark-theme .intro-title {
  color: #eee;
}
.file-meta-info-card {
  margin: 18px auto 0 auto;
  max-width: 600px;
  background: #f8f9fa;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  padding: 18px 24px 12px 24px;
  font-size: 1.02rem;
  color: #333;
}
#app.dark-theme .file-meta-info-card {
  background: #232323;
  color: #eee;
  box-shadow: 0 2px 8px rgba(0,0,0,0.18);
}
.file-meta-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-size: 1.01em;
}
.file-meta-label {
  min-width: 90px;
  color: #888;
  font-weight: 500;
  margin-right: 8px;
}
#app.dark-theme .file-meta-label {
  color: #bbb;
}
.file-meta-link {
  color: #409eff;
  word-break: break-all;
  text-decoration: underline;
}
#app.dark-theme .file-meta-link {
  color: #4a9eff;
}
#app.dark-theme .jv-container {
  background: #232323 !important;
  color: #eee !important;
  border-color: #444 !important;
}
#app.dark-theme .jv-key {
  color: #4a9eff !important;
}
#app.dark-theme .jv-number {
  color: #f39c12 !important;
}
#app.dark-theme .jv-string {
  color: #27ae60 !important;
}
#app.dark-theme .jv-boolean {
  color: #e67e22 !important;
}
#app.dark-theme .jv-null {
  color: #e74c3c !important;
}
#app.jv-container .jv-item.jv-object {
    color: #32ba6d;
}

.feedback-bar {
  width: 100%;
  margin: 0 auto;        /* 居中 */
  text-align: right;
  box-sizing: border-box;
}

@media screen and (max-width: 700px) {
  .feedback-bar {
    max-width: 480px;    /* 和移动端卡片宽度一致 */
    padding-right: 8px;  /* 和卡片内容对齐 */
    padding-left: 8px;
  }
}

.jv-container.jv-light .jv-item.jv-object {
  color: #888;
}

/* 版本号显示样式 */
.version-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  margin-bottom: 20px;
  padding: 0 10px;
}

.version-text {
  font-size: 0.85rem;
  color: #999;
  font-weight: 400;
}

#app.dark-theme .version-text {
  color: #666;
}

.playground-link {
  font-size: 0.85rem;
  color: #409eff;
  text-decoration: none;
}

.playground-link:hover {
  color: #66b1ff;
}

#app.dark-theme .playground-link {
  color: #4a9eff;
}

#app.dark-theme .playground-link:hover {
  color: #66b1ff;
}

/* 认证配置按钮样式 */
.auth-config-btn-active {
  animation: auth-pulse 2s infinite;
}

@keyframes auth-pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.4);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(64, 158, 255, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0);
  }
}

/* 认证配置弹窗暗色模式适配 */
#app.dark-theme .el-dialog {
  background: #2d2d2d;
}

#app.dark-theme .el-dialog__title {
  color: #eee;
}

#app.dark-theme .el-form-item__label {
  color: #ccc;
}
</style>
