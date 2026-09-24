
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



public interface CreateUserMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      ExampleConnector,
      CreateUserMutation.Data,
      CreateUserMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val id: String,
    val name: String,
    val email: String
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val user_insert: UserKey
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "CreateUser"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun CreateUserMutation.ref(
  
    id: String,name: String,email: String,

  
  
): com.google.firebase.dataconnect.MutationRef<
    CreateUserMutation.Data,
    CreateUserMutation.Variables
  > =
  ref(
    
      CreateUserMutation.Variables(
        id=id,name=name,email=email,
  
      )
    
  )

public suspend fun CreateUserMutation.execute(

  
    
      id: String,name: String,email: String,

  

  ): com.google.firebase.dataconnect.MutationResult<
    CreateUserMutation.Data,
    CreateUserMutation.Variables
  > =
  ref(
    
      id=id,name=name,email=email,
  
    
  ).execute()


