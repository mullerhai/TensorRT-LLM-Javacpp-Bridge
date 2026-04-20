#pragma once
// Stub for torch/extension.h
#include <cstdint>
#include <vector>
#include <string>
#include <optional>

namespace at {
class Tensor {
public:
    Tensor() {}
    void* data_ptr() const { return nullptr; }
    int64_t size(int64_t dim) const { return 0; }
    int64_t numel() const { return 0; }
    bool is_contiguous() const { return true; }
    bool is_cuda() const { return false; }
    int64_t dim() const { return 0; }
};
using TensorList = std::vector<Tensor>;
} // namespace at

namespace c10 {
enum class ScalarType : int8_t {
    Byte = 0, Char, Short, Int, Long, Half, Float, Double, ComplexHalf, ComplexFloat, ComplexDouble, Bool, QInt8, QUInt8, QInt32, BFloat16, QUInt4x2, QUInt2x4, Bits1x8, Bits2x4, Bits4x2, Bits8, Bits16, Float8_e5m2, Float8_e4m3fn, Float8_e5m2fnuz, Float8_e4m3fnuz, Undefined, NumOptions
};
using cuda_data_type_t = int;
template <typename T>
using optional = std::optional<T>;
} // namespace c10

namespace torch {
using Tensor = at::Tensor;
using TensorList = at::TensorList;
template <typename T>
using optional = c10::optional<T>;
namespace jit {
class CustomClassHolder {};
} // namespace jit
} // namespace torch

namespace th = torch;


