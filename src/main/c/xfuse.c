#include "xorfilter.h"
#include "binaryfusefilter.h"

// Wrapper functions for xor8

bool xfuse_xor8_allocate(uint32_t size, xor8_t *filter) {
    return xor8_allocate(size, filter);
}

bool xfuse_xor8_populate(uint64_t *keys, uint32_t size, xor8_t *filter) {
    return xor8_populate(keys, size, filter);
}

bool xfuse_xor8_contain(uint64_t key, const xor8_t *filter) {
    return xor8_contain(key, filter);
}

void xfuse_xor8_free(xor8_t *filter) {
    xor8_free(filter);
}

size_t xfuse_xor8_size_in_bytes(const xor8_t *filter) {
    return xor8_size_in_bytes(filter);
}

// Wrapper functions for xor16

bool xfuse_xor16_allocate(uint32_t size, xor16_t *filter) {
    return xor16_allocate(size, filter);
}

bool xfuse_xor16_populate(uint64_t *keys, uint32_t size, xor16_t *filter) {
    return xor16_populate(keys, size, filter);
}

bool xfuse_xor16_contain(uint64_t key, const xor16_t *filter) {
    return xor16_contain(key, filter);
}

void xfuse_xor16_free(xor16_t *filter) {
    xor16_free(filter);
}

size_t xfuse_xor16_size_in_bytes(const xor16_t *filter) {
    return xor16_size_in_bytes(filter);
}

// Wrapper functions for binary_fuse8

bool xfuse_binary_fuse8_allocate(uint32_t size, binary_fuse8_t *filter) {
    return binary_fuse8_allocate(size, filter);
}

bool xfuse_binary_fuse8_populate(uint64_t *keys, uint32_t size, binary_fuse8_t *filter) {
    return binary_fuse8_populate(keys, size, filter);
}

bool xfuse_binary_fuse8_contain(uint64_t key, const binary_fuse8_t *filter) {
    return binary_fuse8_contain(key, filter);
}

void xfuse_binary_fuse8_free(binary_fuse8_t *filter) {
    binary_fuse8_free(filter);
}

size_t xfuse_binary_fuse8_size_in_bytes(const binary_fuse8_t *filter) {
    return binary_fuse8_size_in_bytes(filter);
}

bool xfuse_binary_fuse16_allocate(uint32_t size, binary_fuse16_t *filter) {
    return binary_fuse16_allocate(size, filter);
}

bool xfuse_binary_fuse16_populate(uint64_t *keys, uint32_t size, binary_fuse16_t *filter) {
    return binary_fuse16_populate(keys, size, filter);
}

bool xfuse_binary_fuse16_contain(uint64_t key, const binary_fuse16_t *filter) {
    return binary_fuse16_contain(key, filter);
}

void xfuse_binary_fuse16_free(binary_fuse16_t *filter) {
    binary_fuse16_free(filter);
}

size_t xfuse_binary_fuse16_size_in_bytes(const binary_fuse16_t *filter) {
    return binary_fuse16_size_in_bytes(filter);
}