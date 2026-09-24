
@file:Suppress(
  "KotlinRedundantDiagnosticSuppress",
  "LocalVariableName",
  "MayBeConstant",
  "RedundantVisibilityModifier",
  "RedundantCompanionReference",
  "RemoveEmptyClassBody",
  "SpellCheckingInspection",
  "LocalVariableName",
  "unused",
)

package com.erns.alertauni.generated



public interface RegistrarNuevaFrutaMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      ExampleConnector,
      RegistrarNuevaFrutaMutation.Data,
      RegistrarNuevaFrutaMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val id: String,
    val nombre: String
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val fruta_insert: FrutaKey
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "RegistrarNuevaFruta"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun RegistrarNuevaFrutaMutation.ref(
  
    id: String,nombre: String,

  
  
): com.google.firebase.dataconnect.MutationRef<
    RegistrarNuevaFrutaMutation.Data,
    RegistrarNuevaFrutaMutation.Variables
  > =
  ref(
    
      RegistrarNuevaFrutaMutation.Variables(
        id=id,nombre=nombre,
  
      )
    
  )

public suspend fun RegistrarNuevaFrutaMutation.execute(

  
    
      id: String,nombre: String,

  

  ): com.google.firebase.dataconnect.MutationResult<
    RegistrarNuevaFrutaMutation.Data,
    RegistrarNuevaFrutaMutation.Variables
  > =
  ref(
    
      id=id,nombre=nombre,
  
    
  ).execute()


