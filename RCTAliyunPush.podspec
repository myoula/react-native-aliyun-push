require "json"
version = JSON.parse(File.read("package.json"))["version"]

Pod::Spec.new do |spec|

  spec.name         = "RCTAliyunPush"
  spec.version      = version
  spec.summary      = "A short description of RCTAliyunPush."
  spec.homepage     = "https://github.com/hengkx/react-native-aliyun-push"
  spec.license      = "MIT"
  spec.author             = { "wwwlin" => "188658587@qq.com" }
  spec.ios.deployment_target = "9.0"
  spec.tvos.deployment_target = "9.0"
  spec.source         = { :git => 'https://github.com/hengkx/react-native-aliyun-push.git', :tag => "v#{spec.version}"}
  spec.source_files  =  "ios/**/*.{h,m}"
  spec.dependency 'AlicloudPush', '~> 1.9.9'
  spec.libraries = "z", "resolv", "sqlite3"

  spec.requires_arc = true

  spec.dependency "React-Core"
end
