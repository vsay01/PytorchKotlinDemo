# PytorchKotlinDemo

This is an Android project written in Kotlin to show a simple image classification application that uses Android PyTorch API and a trained PyTorch model.

In this demo application, user can either upload a picture or take photo. Then run the image analysis on the picture.

## Architecture

I followed one of the [android google architecture component sample](https://github.com/android/architecture-components-samples/tree/master/LiveDataSample).

This sample showcases the following Architecture Components:

* [LiveData](https://developer.android.com/reference/android/arch/lifecycle/LiveData.html)
* [ViewModels](https://developer.android.com/reference/android/arch/lifecycle/ViewModel.html)
* [Data Binding](https://developer.android.com/topic/libraries/data-binding)

## Serialize a PyTorch model

In [Android demo github](https://github.com/pytorch/android-demo-app), it describe in detail how the PyTorch model generated. 

We cannot use the saved model directly in the notebook, we need to serialize that saved model.

In the Jupyter notebook where I trained the model, I can do the following


```
//load the model
ckp_path = './best_model.pt'
if(use_cuda):
  checkpoint = torch.load(ckp_path)
else:
  checkpoint = torch.load(ckp_path, map_location=torch.device('cpu'))
loaded_model.load_state_dict(checkpoint['state_dict'])
```

```
//serialize the model
loaded_model.eval()
example = torch.rand(1, 3, 224, 224)
if use_cuda:
  example = example.cuda()
traced_script_module = torch.jit.trace(loaded_model, example)
traced_script_module.save("./serialized_model.pt")
```

After this operations, we should have a usable model, serialized_model.pt.

## References

[Android demo github](https://github.com/pytorch/android-demo-app)

[PyTorch Mobile](https://pytorch.org/mobile/home/)

[android google architecture component sample](https://github.com/android/architecture-components-samples/tree/master/LiveDataSample)
