SHELL := /bin/bash

ROOT := $(abspath $(dir $(lastword $(MAKEFILE_LIST)))/../..)
PRODUCT ?= nova-trunk_staging-eng
SOONG_ALLOW_MISSING_DEPENDENCIES ?= true
APK ?=
LOG ?= /tmp/nova.log
APK_GOALS := $(filter-out help framework native all run run-log test-ipc daemon,$(MAKECMDGOALS))
APK_INPUT := $(strip $(if $(APK),$(APK),$(APK_GOALS)))

HOST_OUT := $(ROOT)/out/host/linux-x86
NOVA_BIN := $(HOST_OUT)/bin/nova
LD_PATH := $(HOST_OUT)/lib64:$(HOST_OUT)/lib
LD_PRELOAD_LIB := $(HOST_OUT)/lib64/libdeepbind_wrapper.so

FRAMEWORK_TARGETS := nova-framework-host
NATIVE_TARGETS := \
	libandroid-nova \
	libOpenSLES-nova \
	libdeepbind_wrapper \
	libGLESv3-nova \
	libnova_android \
	libnova_egl \
	libnova_jni \
	libnova_ipc \
	libnova_binder_transport \
	nova \
	nova-daemon \
	nova_ipc_test

ENVSETUP = source build/envsetup.sh && lunch $(PRODUCT)
BUILD_ENV = export SOONG_ALLOW_MISSING_DEPENDENCIES=$(SOONG_ALLOW_MISSING_DEPENDENCIES)
RUN_ENV = export LD_LIBRARY_PATH=$(LD_PATH) && export LD_PRELOAD=$(LD_PRELOAD_LIB)

DAEMON_BIN := $(HOST_OUT)/bin/nova-daemon
IPCTEST_BIN := $(HOST_OUT)/bin/nova_ipc_test

.PHONY: help framework native all run run-log test-ipc daemon

%:
	@:

help:
	@printf '%s\n' \
	'Nova helpers' \
	'' \
	'Usage:' \
	'  make -f vendor/nova/Makefile framework' \
	'  make -f vendor/nova/Makefile native' \
	'  make -f vendor/nova/Makefile all' \
	'  make -f vendor/nova/Makefile run APK=/abs/path/app.apk' \
	'  make -f vendor/nova/Makefile run-log APK=/abs/path/app.apk LOG=/tmp/nova.log' \
	'' \
	'Variables:' \
	'  PRODUCT=<lunch target> default: $(PRODUCT)' \
	'  SOONG_ALLOW_MISSING_DEPENDENCIES=<true|false> default: $(SOONG_ALLOW_MISSING_DEPENDENCIES)' \
	'  APK=/abs/path/app.apk' \
	'  LOG=/tmp/nova.log'

framework:
	cd $(ROOT) && $(ENVSETUP) >/tmp/nova-lunch.log && $(BUILD_ENV) && m --soong-only $(FRAMEWORK_TARGETS)

native:
	cd $(ROOT) && $(ENVSETUP) >/tmp/nova-lunch.log && $(BUILD_ENV) && m --soong-only $(NATIVE_TARGETS)

all: framework native

run:
	@test -n "$(APK_INPUT)" || { echo 'Usage: make run APK=/abs/path/app.apk or make run path/to/app.apk'; exit 2; }
	@test -x "$(NOVA_BIN)" || { echo 'Missing nova binary: $(NOVA_BIN)'; exit 2; }
	@test -f "$(LD_PRELOAD_LIB)" || { echo 'Missing preload library: $(LD_PRELOAD_LIB)'; exit 2; }
	@APK_PATH="$$(realpath -m -- "$(APK_INPUT)")"; \
	test -f "$$APK_PATH" || { echo "APK not found: $$APK_PATH"; exit 2; }; \
	cd $(ROOT) && $(RUN_ENV) && "$(NOVA_BIN)" "$$APK_PATH"

test-ipc:
	@test -x "$(IPCTEST_BIN)" || { echo 'Missing nova_ipc_test binary: $(IPCTEST_BIN)'; exit 2; }
	cd $(ROOT) && "$(IPCTEST_BIN)"

daemon:
	@test -x "$(DAEMON_BIN)" || { echo 'Missing nova-daemon binary: $(DAEMON_BIN)'; exit 2; }
	cd $(ROOT) && $(RUN_ENV) && "$(DAEMON_BIN)"

run-log:
	@test -n "$(APK_INPUT)" || { echo 'Usage: make run-log APK=/abs/path/app.apk or make run-log path/to/app.apk'; exit 2; }
	@test -x "$(NOVA_BIN)" || { echo 'Missing nova binary: $(NOVA_BIN)'; exit 2; }
	@test -f "$(LD_PRELOAD_LIB)" || { echo 'Missing preload library: $(LD_PRELOAD_LIB)'; exit 2; }
	@APK_PATH="$$(realpath -m -- "$(APK_INPUT)")"; \
	test -f "$$APK_PATH" || { echo "APK not found: $$APK_PATH"; exit 2; }; \
	cd $(ROOT) && $(RUN_ENV) && "$(NOVA_BIN)" "$$APK_PATH" >"$(LOG)" 2>&1
