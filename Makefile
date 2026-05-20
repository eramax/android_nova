SHELL := /bin/bash

ROOT := $(abspath $(dir $(lastword $(MAKEFILE_LIST)))/../..)
PRODUCT ?= nova-trunk_staging-eng
SOONG_ALLOW_MISSING_DEPENDENCIES ?= true
APK ?=
LOG ?= /tmp/nova.log

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
	nova

ENVSETUP = source build/envsetup.sh && lunch $(PRODUCT)
BUILD_ENV = export SOONG_ALLOW_MISSING_DEPENDENCIES=$(SOONG_ALLOW_MISSING_DEPENDENCIES)
RUN_ENV = export LD_LIBRARY_PATH=$(LD_PATH) && export LD_PRELOAD=$(LD_PRELOAD_LIB)

.PHONY: help framework native all run run-log

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
	cd $(ROOT) && $(ENVSETUP) >/tmp/nova-lunch.log && $(BUILD_ENV) && m $(FRAMEWORK_TARGETS)

native:
	cd $(ROOT) && $(ENVSETUP) >/tmp/nova-lunch.log && $(BUILD_ENV) && m $(NATIVE_TARGETS)

all: framework native

run:
	@test -n "$(APK)" || { echo 'APK=/abs/path/app.apk is required'; exit 2; }
	@test -x "$(NOVA_BIN)" || { echo 'Missing nova binary: $(NOVA_BIN)'; exit 2; }
	@test -f "$(LD_PRELOAD_LIB)" || { echo 'Missing preload library: $(LD_PRELOAD_LIB)'; exit 2; }
	cd $(ROOT) && $(RUN_ENV) && "$(NOVA_BIN)" "$(APK)"

run-log:
	@test -n "$(APK)" || { echo 'APK=/abs/path/app.apk is required'; exit 2; }
	@test -x "$(NOVA_BIN)" || { echo 'Missing nova binary: $(NOVA_BIN)'; exit 2; }
	@test -f "$(LD_PRELOAD_LIB)" || { echo 'Missing preload library: $(LD_PRELOAD_LIB)'; exit 2; }
	cd $(ROOT) && $(RUN_ENV) && "$(NOVA_BIN)" "$(APK)" >"$(LOG)" 2>&1
