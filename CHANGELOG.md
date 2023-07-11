# Changelog

## Unreleased

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## 2.0.2

### Changed
- Library endpoint search 对库里的接口搜索调整

## 2.0.1

### Fixed
- Search Endpoint bug 打开多个项目搜索接口冲突的问题
- Code generator dialog validate bug 代码生成器的校验问题

## 2.0.0

### Added
- Endpoint search everywhere 接口路径搜索功能上线

### Changed
- Upgrade JDK version to 17 升级编译的 JDK 版本
- Upgrade IDEA since version to 222 升级 IDEA 的最低版本为 2022.2

## 1.8.1

### Fixed
- Scan endpoint bug fix 扫描某些项目读取不到接口的问题修复
- Popup menu bug fix 接口路径右键复制问题修复

## 1.8.0

### Added
- Copy endpoint path 右键可复制接口路径

### Fixed
- endpoint tree text bug fix 修复接口目录树文字展示不出来的问题

## 1.7.2

### Fixed
- endpoint tree text bug fix 修复接口目录树文字展示不出来的问题

## 1.7.1

### Changed
- endpoint tree text color 接口路径树文本颜色美化

### Fixed
- default method endpoint 修复默认方法没有被读取到接口列表的问题

## 1.7.0

### Changed
- Use new logo 使用新的图标
- Merge `page` and `listAll` in generator 在生成器中将 `page` 和 `listAll` 方法对应的 xml 方法合并
- Use `BatchUtil` in generator 在生成器中使用 `BatchUtil` 包装批量操作的方法

### Fixed
- generator bug 生成器的某些缺陷修复

## 1.6.0

### Added
- Queue Event Listener navigate 队列事件与监听之间支持导航
- New version first start notifier 更新后首次打开增加通知

## 1.5.1

### Added
- Support IDEA 2023.1

## 1.5.0

### Added
- Local event listener navigate

### Changed
- Task listener navigation support multiple

## 1.4.2

### Added
- Mybatis generator support no primary key

## 1.4.1

### Fixed
- Mybatis generate service extends bug
- Mybatis generate excel service
- Mybatis generate transform function

## 1.4.0

### Added
- Mybatis generate extends EFEntityBase
- Mybatis generator template interface chooser
- Convert doc comment to Swagger annotation

## 1.3.1

### Added
- Mybatis generator datasource validate
- Mybatis generator input validate
- Mybatis generate success notifier navigate

### Changed
- git ignore

### Fixed
- Endpoint path "//" bug

## 1.3.0

### Added
- EF code generate

## 1.2.1

### Added
- Entity support navigate to mapper
- Use changelog Plugin build notes

### Fixed
- Listener navigate bug fix
