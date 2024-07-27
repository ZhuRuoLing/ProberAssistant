#!/bin/bash

source ./env.sh

gomobile init
go get -u golang.org/x/mobile
export PATH=$PATH:/Users/zhuruoling/Library/Java/JavaVirtualMachines/temurin-21.0.3/Contents/Home

echo $PATH

BUILD=".build"

mkdir -p $BUILD

rm -rf $BUILD/*

gomobile bind -x -v -androidapi 21 -cache $(realpath $BUILD) -trimpath -ldflags='-s -w' . || exit 1
rm -r libprob-sources.jar

proj=../app/libs
mkdir -p $proj
cp -f libprob.aar $proj
echo ">> install $(realpath $proj)/libcore.aar"
