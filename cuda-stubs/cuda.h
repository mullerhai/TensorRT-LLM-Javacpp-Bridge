#pragma once
#include <cstdint>
#include <cstddef>

// CUDA Driver API stub types
typedef unsigned long long CUdeviceptr;
typedef int CUdevice;
typedef void* CUcontext;
typedef void* CUmodule;
typedef void* CUfunction;
typedef void* CUstream_st;
typedef CUstream_st* CUstream;
typedef void* CUevent;
typedef unsigned long long CUmemGenericAllocationHandle;

enum CUmemAllocationHandleType {
    CU_MEM_HANDLE_TYPE_NONE = 0,
    CU_MEM_HANDLE_TYPE_POSIX_FILE_DESCRIPTOR = 1,
    CU_MEM_HANDLE_TYPE_WIN32 = 2,
    CU_MEM_HANDLE_TYPE_WIN32_KMT = 4,
    CU_MEM_HANDLE_TYPE_FABRIC = 8
};

enum CUmemLocationType {
    CU_MEM_LOCATION_TYPE_INVALID = 0,
    CU_MEM_LOCATION_TYPE_DEVICE = 1,
    CU_MEM_LOCATION_TYPE_HOST = 2,
    CU_MEM_LOCATION_TYPE_HOST_NUMA = 3,
    CU_MEM_LOCATION_TYPE_HOST_NUMA_CURRENT = 4
};

struct CUmemLocation {
    CUmemLocationType type;
    int id;
};

enum CUmemAccessFlags {
    CU_MEM_ACCESS_FLAGS_PROT_NONE = 0,
    CU_MEM_ACCESS_FLAGS_PROT_READ = 1,
    CU_MEM_ACCESS_FLAGS_PROT_READWRITE = 3
};

struct CUmemAccessDesc {
    CUmemLocation location;
    CUmemAccessFlags flags;
};

enum CUmemAllocationType {
    CU_MEM_ALLOCATION_TYPE_INVALID = 0,
    CU_MEM_ALLOCATION_TYPE_PINNED = 1,
    CU_MEM_ALLOCATION_TYPE_MAX = 0xFFFFFFFF
};

struct CUmemAllocationProp {
    CUmemAllocationType type;
    CUmemAllocationHandleType requestedHandleTypes;
    CUmemLocation location;
    void* win32HandleMetaData;
    struct { unsigned char compressionType; unsigned char gpuDirectRDMACapable; unsigned short usage; } allocFlags;
};

typedef int CUresult;
#define CUDA_SUCCESS 0

// Minimal driver API function stubs
inline CUresult cuMemCreate(CUmemGenericAllocationHandle* handle, size_t size, const CUmemAllocationProp* prop, unsigned long long flags) { return CUDA_SUCCESS; }
inline CUresult cuMemRelease(CUmemGenericAllocationHandle handle) { return CUDA_SUCCESS; }
inline CUresult cuMemAddressReserve(CUdeviceptr* ptr, size_t size, size_t alignment, CUdeviceptr addr, unsigned long long flags) { return CUDA_SUCCESS; }
inline CUresult cuMemAddressFree(CUdeviceptr ptr, size_t size) { return CUDA_SUCCESS; }
inline CUresult cuMemMap(CUdeviceptr ptr, size_t size, size_t offset, CUmemGenericAllocationHandle handle, unsigned long long flags) { return CUDA_SUCCESS; }
inline CUresult cuMemUnmap(CUdeviceptr ptr, size_t size) { return CUDA_SUCCESS; }
inline CUresult cuMemSetAccess(CUdeviceptr ptr, size_t size, const CUmemAccessDesc* desc, size_t count) { return CUDA_SUCCESS; }
inline CUresult cuDeviceGet(CUdevice* device, int ordinal) { return CUDA_SUCCESS; }
inline CUresult cuDeviceGetCount(int* count) { return CUDA_SUCCESS; }

