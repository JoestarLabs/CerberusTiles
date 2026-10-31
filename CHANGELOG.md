# Changelog

## [0.5.2](https://github.com/JoestarLabs/CerberusTiles/compare/v0.5.1...v0.5.2) (2026-10-31)


### Features

* add Permissions & Privacy screen with automated README sync ([1c9b63d](https://github.com/JoestarLabs/CerberusTiles/commit/1c9b63dee14ff1757788c49e0d71e11c1bb588b0))
* add Smart Pixels OLED battery saver ([d19f648](https://github.com/JoestarLabs/CerberusTiles/commit/d19f648762e0a50317bbafd067cb51f8ad235ef6))
* **navigation:** migrate to multi-activity architecture for native screen transitions ([f1ed449](https://github.com/JoestarLabs/CerberusTiles/commit/f1ed4496e27c2e26874079c7dc23f16502726835))
* **network:** add native network speed limitation toggle and QS tile categorization ([5278b0e](https://github.com/JoestarLabs/CerberusTiles/commit/5278b0e6718c0f979bb9fec580c5e53080cd9e07))
* **ui:** add leading icons to adaptive brightness, charging, and smart pixels switches ([718f5d1](https://github.com/JoestarLabs/CerberusTiles/commit/718f5d18a6a0236b62ad6ee66088c40c518a8a62))
* **ui:** adopt battery shield and network check icons for charging and speed limit ([60549ba](https://github.com/JoestarLabs/CerberusTiles/commit/60549bae5bfaa7668fdc77b52d07cd2fc93b7901))
* **ui:** adopt minimalist grouped card containers for settings entries ([3dc28d7](https://github.com/JoestarLabs/CerberusTiles/commit/3dc28d78f43da1de5b3abd7236496c039091584b))
* **ui:** consolidate entire icon suite to Material Symbols Rounded ([72b808e](https://github.com/JoestarLabs/CerberusTiles/commit/72b808e4189075a2ffd7b1e44df20f82c3673661))
* **ui:** modernize About settings item and dialog with JoestarLabs ecosystem links ([612573a](https://github.com/JoestarLabs/CerberusTiles/commit/612573aa2d52341eb57acd3819e4802c84c459dc))
* **ui:** replace brightness slider with segmented preset buttons ([5960dab](https://github.com/JoestarLabs/CerberusTiles/commit/5960dabd27c90274cb0876d2cc36c4e7b6e401de))
* **ui:** unify PermissionsActivity, LicensesActivity, and PermissionsScreen with Material 3 Expressive ([5a5053b](https://github.com/JoestarLabs/CerberusTiles/commit/5a5053b6b2725bc7dad45057f2947693b5743af1))
* **ui:** upgrade SettingsActivity top app bar to collapsing MediumTopAppBar ([95d3cbe](https://github.com/JoestarLabs/CerberusTiles/commit/95d3cbe9534db71f993ed2ee73d6be1881ff9faa))


### Bug Fixes

* **activities:** guard tile preferences redirect in MainActivity against recreation ([a62be2c](https://github.com/JoestarLabs/CerberusTiles/commit/a62be2c9742dcbfb7985c523a4e920af24d1315f))
* **deps:** update dependency androidx.compose.material3:material3 to v1.5.0-beta02 ([479a569](https://github.com/JoestarLabs/CerberusTiles/commit/479a569d2db9a9ceb5b6bb963351b841880a5567))
* **deps:** update dependency androidx.compose.material3:material3 to v1.5.0-beta02 ([3489521](https://github.com/JoestarLabs/CerberusTiles/commit/3489521bd5191a24068366231bfa2874ac106620))
* **deps:** update gradle dependencies ([4f4aa61](https://github.com/JoestarLabs/CerberusTiles/commit/4f4aa611b37921c05026bd64207f64dda36d9215))
* **deps:** update gradle dependencies ([b38cb17](https://github.com/JoestarLabs/CerberusTiles/commit/b38cb17ef41fe060d83d535b57149daa515f56ae))
* **deps:** update gradle dependencies to v15.3.0 ([4fa923c](https://github.com/JoestarLabs/CerberusTiles/commit/4fa923c53b050d010d751d2cfe6bc2acbe5ec9dd))
* **deps:** update gradle dependencies to v15.3.0 ([b72beac](https://github.com/JoestarLabs/CerberusTiles/commit/b72beac7ce31438785221406cdc003873455acea))
* **network:** handle invalid speed limit values and declare settings package query ([996b780](https://github.com/JoestarLabs/CerberusTiles/commit/996b7804211d22817b6f998d3ec49cc229a2b4e5))
* **ui:** disable brightness preset selection while adaptive brightness is active ([6b332a3](https://github.com/JoestarLabs/CerberusTiles/commit/6b332a317fc15ea02bf623a96c3825c2d8e527a4))
* **ui:** eliminate double dialog nesting and fix ringer mode state propagation in OverlayDialog ([a6ed779](https://github.com/JoestarLabs/CerberusTiles/commit/a6ed779bff5a53cfdc7b5ad28166142c5009d424))
* **ui:** explicitly disable adaptive brightness and update state on write success ([68fabd7](https://github.com/JoestarLabs/CerberusTiles/commit/68fabd7f7f7a101fc2771bc1c0f35d5190bb6b06))
* **ui:** guard brightness preset selection behind write permissions and write success ([818cbb7](https://github.com/JoestarLabs/CerberusTiles/commit/818cbb749dee87b9c095735040bbccbe86872c29))
* **ui:** pass adaptive brightness state in BrightnessSlider ([1a52558](https://github.com/JoestarLabs/CerberusTiles/commit/1a52558a0fce1620428c214dca4380b62a79ee9a))
* **ui:** refresh brightness preset on adaptive toggle and provide inline turn-off action ([0d7fd56](https://github.com/JoestarLabs/CerberusTiles/commit/0d7fd5642caf5e24c484497cb71b625b26cccfec))
* **ui:** smooth permissions scrolling and streamline brightness preset caption ([e6b4a49](https://github.com/JoestarLabs/CerberusTiles/commit/e6b4a497a480a1ff33edbea12e93ca717bb1f86d))

## [0.5.1](https://github.com/JoestarLabs/CerberusTiles/compare/v0.5.0...v0.5.1) (2026-10-10)


### Bug Fixes

* bind charging optimization directly to LiveData state to prevent premature switch toggles without permission ([19603f7](https://github.com/JoestarLabs/CerberusTiles/commit/19603f73948fef17f6d4472bacc31fe0afff659d))
* **build:** resolve build failure for Gradle 9.8 and dependency updates ([a956285](https://github.com/JoestarLabs/CerberusTiles/commit/a956285e62a39d57529657e70a86ccb9c5828dee))
* **ci:** derive versionCode from versionName and remove CI version override ([5a9cab6](https://github.com/JoestarLabs/CerberusTiles/commit/5a9cab64769bcc34804b3d0a0992f93f5b1b7350))
* **deps:** update gradle dependencies ([03096d8](https://github.com/JoestarLabs/CerberusTiles/commit/03096d8bb42504e8569838afcc1bc5bcb5614c90))
* **deps:** update gradle dependencies ([d4b107f](https://github.com/JoestarLabs/CerberusTiles/commit/d4b107f1db35ae7e44c2d0ea80bd7599c327c84a))
* enable resource shrinking ([8cddcdb](https://github.com/JoestarLabs/CerberusTiles/commit/8cddcdb5d6495f98030426f21c20b0d3298d9963))
* update charging optimization toggle state optimistically and sync via LaunchedEffect ([4802df9](https://github.com/JoestarLabs/CerberusTiles/commit/4802df9ea2ed8531c3adc2faae9289179bbb84fb))
