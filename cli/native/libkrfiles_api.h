#ifndef KONAN_LIBKRFILES_H
#define KONAN_LIBKRFILES_H
#ifdef __cplusplus
extern "C" {
#endif
#ifdef __cplusplus
typedef bool            libkrfiles_KBoolean;
#else
typedef _Bool           libkrfiles_KBoolean;
#endif
typedef unsigned short     libkrfiles_KChar;
typedef signed char        libkrfiles_KByte;
typedef short              libkrfiles_KShort;
typedef int                libkrfiles_KInt;
typedef long long          libkrfiles_KLong;
typedef unsigned char      libkrfiles_KUByte;
typedef unsigned short     libkrfiles_KUShort;
typedef unsigned int       libkrfiles_KUInt;
typedef unsigned long long libkrfiles_KULong;
typedef float              libkrfiles_KFloat;
typedef double             libkrfiles_KDouble;
typedef float __attribute__ ((__vector_size__ (16))) libkrfiles_KVector128;
typedef void*              libkrfiles_KNativePtr;
struct libkrfiles_KType;
typedef struct libkrfiles_KType libkrfiles_KType;

typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlin_Byte;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlin_Short;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlin_Int;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlin_Long;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlin_Float;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlin_Double;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlin_Char;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlin_Boolean;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlin_Unit;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlin_UByte;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlin_UShort;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlin_UInt;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlin_ULong;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_AuthStorage;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_AuthManager;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_AuthManager_Companion;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_ServerCredentials;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlin_Any;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_FilebrowserClient;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_io_ktor_client_HttpClient;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_Resource;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlin_collections_List;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_Sorting;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_Resource_$serializer;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlinx_serialization_descriptors_SerialDescriptor;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlin_Array;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlinx_serialization_encoding_Decoder;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlinx_serialization_encoding_Encoder;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_Resource_Companion;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_kotlinx_serialization_KSerializer;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_Sorting_$serializer;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_Sorting_Companion;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_SearchResult;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_SearchResult_$serializer;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_SearchResult_Companion;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_User;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_Permissions;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_User_$serializer;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_User_Companion;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_Permissions_$serializer;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_Permissions_Companion;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_UserData;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_UserData_$serializer;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_UserData_Companion;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError_$serializer;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError_Companion;
typedef struct {
  libkrfiles_KNativePtr pinned;
} libkrfiles_kref_dev_rolandh_krfiles_FilebrowserException;


typedef struct {
  /* Service functions. */
  void (*DisposeStablePointer)(libkrfiles_KNativePtr ptr);
  void (*DisposeString)(const char* string);
  libkrfiles_KBoolean (*IsInstance)(libkrfiles_KNativePtr ref, const libkrfiles_KType* type);
  libkrfiles_kref_kotlin_Byte (*createNullableByte)(libkrfiles_KByte);
  libkrfiles_KByte (*getNonNullValueOfByte)(libkrfiles_kref_kotlin_Byte);
  libkrfiles_kref_kotlin_Short (*createNullableShort)(libkrfiles_KShort);
  libkrfiles_KShort (*getNonNullValueOfShort)(libkrfiles_kref_kotlin_Short);
  libkrfiles_kref_kotlin_Int (*createNullableInt)(libkrfiles_KInt);
  libkrfiles_KInt (*getNonNullValueOfInt)(libkrfiles_kref_kotlin_Int);
  libkrfiles_kref_kotlin_Long (*createNullableLong)(libkrfiles_KLong);
  libkrfiles_KLong (*getNonNullValueOfLong)(libkrfiles_kref_kotlin_Long);
  libkrfiles_kref_kotlin_Float (*createNullableFloat)(libkrfiles_KFloat);
  libkrfiles_KFloat (*getNonNullValueOfFloat)(libkrfiles_kref_kotlin_Float);
  libkrfiles_kref_kotlin_Double (*createNullableDouble)(libkrfiles_KDouble);
  libkrfiles_KDouble (*getNonNullValueOfDouble)(libkrfiles_kref_kotlin_Double);
  libkrfiles_kref_kotlin_Char (*createNullableChar)(libkrfiles_KChar);
  libkrfiles_KChar (*getNonNullValueOfChar)(libkrfiles_kref_kotlin_Char);
  libkrfiles_kref_kotlin_Boolean (*createNullableBoolean)(libkrfiles_KBoolean);
  libkrfiles_KBoolean (*getNonNullValueOfBoolean)(libkrfiles_kref_kotlin_Boolean);
  libkrfiles_kref_kotlin_Unit (*createNullableUnit)(void);
  libkrfiles_kref_kotlin_UByte (*createNullableUByte)(libkrfiles_KUByte);
  libkrfiles_KUByte (*getNonNullValueOfUByte)(libkrfiles_kref_kotlin_UByte);
  libkrfiles_kref_kotlin_UShort (*createNullableUShort)(libkrfiles_KUShort);
  libkrfiles_KUShort (*getNonNullValueOfUShort)(libkrfiles_kref_kotlin_UShort);
  libkrfiles_kref_kotlin_UInt (*createNullableUInt)(libkrfiles_KUInt);
  libkrfiles_KUInt (*getNonNullValueOfUInt)(libkrfiles_kref_kotlin_UInt);
  libkrfiles_kref_kotlin_ULong (*createNullableULong)(libkrfiles_KULong);
  libkrfiles_KULong (*getNonNullValueOfULong)(libkrfiles_kref_kotlin_ULong);

  /* User functions. */
  struct {
    struct {
      struct {
        struct {
          struct {
            struct {
              struct {
                libkrfiles_KType* (*_type)(void);
                libkrfiles_kref_dev_rolandh_krfiles_AuthManager_Companion (*_instance)();
                libkrfiles_kref_dev_rolandh_krfiles_AuthManager (*create)(libkrfiles_kref_dev_rolandh_krfiles_AuthManager_Companion thiz);
              } Companion;
              libkrfiles_KType* (*_type)(void);
              libkrfiles_kref_dev_rolandh_krfiles_AuthManager (*AuthManager)(libkrfiles_kref_dev_rolandh_krfiles_AuthStorage storage);
            } AuthManager;
            struct {
              libkrfiles_KType* (*_type)(void);
              libkrfiles_kref_dev_rolandh_krfiles_ServerCredentials (*ServerCredentials)(const char* serverUrl, const char* token);
              const char* (*get_serverUrl)(libkrfiles_kref_dev_rolandh_krfiles_ServerCredentials thiz);
              const char* (*get_token)(libkrfiles_kref_dev_rolandh_krfiles_ServerCredentials thiz);
              const char* (*component1)(libkrfiles_kref_dev_rolandh_krfiles_ServerCredentials thiz);
              const char* (*component2)(libkrfiles_kref_dev_rolandh_krfiles_ServerCredentials thiz);
              libkrfiles_kref_dev_rolandh_krfiles_ServerCredentials (*copy)(libkrfiles_kref_dev_rolandh_krfiles_ServerCredentials thiz, const char* serverUrl, const char* token);
              libkrfiles_KBoolean (*equals)(libkrfiles_kref_dev_rolandh_krfiles_ServerCredentials thiz, libkrfiles_kref_kotlin_Any other);
              libkrfiles_KInt (*hashCode)(libkrfiles_kref_dev_rolandh_krfiles_ServerCredentials thiz);
              const char* (*toString)(libkrfiles_kref_dev_rolandh_krfiles_ServerCredentials thiz);
            } ServerCredentials;
            struct {
              libkrfiles_KType* (*_type)(void);
            } AuthStorage;
            struct {
              libkrfiles_KType* (*_type)(void);
              libkrfiles_kref_dev_rolandh_krfiles_FilebrowserClient (*FilebrowserClient)(const char* baseUrl, libkrfiles_kref_io_ktor_client_HttpClient httpClient);
              libkrfiles_KBoolean (*get_isAuthenticated)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserClient thiz);
              void (*close)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserClient thiz);
              void (*logout)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserClient thiz);
              void (*setToken)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserClient thiz, const char* token);
            } FilebrowserClient;
            struct {
              struct {
                libkrfiles_KType* (*_type)(void);
                libkrfiles_kref_dev_rolandh_krfiles_Resource_$serializer (*_instance)();
                libkrfiles_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libkrfiles_kref_dev_rolandh_krfiles_Resource_$serializer thiz);
                libkrfiles_kref_kotlin_Array (*childSerializers)(libkrfiles_kref_dev_rolandh_krfiles_Resource_$serializer thiz);
                libkrfiles_kref_dev_rolandh_krfiles_Resource (*deserialize)(libkrfiles_kref_dev_rolandh_krfiles_Resource_$serializer thiz, libkrfiles_kref_kotlinx_serialization_encoding_Decoder decoder);
                void (*serialize)(libkrfiles_kref_dev_rolandh_krfiles_Resource_$serializer thiz, libkrfiles_kref_kotlinx_serialization_encoding_Encoder encoder, libkrfiles_kref_dev_rolandh_krfiles_Resource value);
              } $serializer;
              struct {
                libkrfiles_KType* (*_type)(void);
                libkrfiles_kref_dev_rolandh_krfiles_Resource_Companion (*_instance)();
                libkrfiles_kref_kotlinx_serialization_KSerializer (*serializer)(libkrfiles_kref_dev_rolandh_krfiles_Resource_Companion thiz);
              } Companion;
              libkrfiles_KType* (*_type)(void);
              libkrfiles_kref_dev_rolandh_krfiles_Resource (*Resource)(const char* name, libkrfiles_KDouble size, const char* extension, const char* modified, libkrfiles_KDouble mode, libkrfiles_KBoolean isDir, libkrfiles_KBoolean isSymlink, const char* type, const char* path, libkrfiles_kref_kotlin_collections_List items, libkrfiles_KInt numDirs, libkrfiles_KInt numFiles, libkrfiles_kref_dev_rolandh_krfiles_Sorting sorting);
              const char* (*get_extension)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_KBoolean (*get_isDir)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_KBoolean (*get_isSymlink)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_kref_kotlin_collections_List (*get_items)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_KDouble (*get_mode)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              const char* (*get_modified)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              const char* (*get_name)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_KInt (*get_numDirs)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_KInt (*get_numFiles)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              const char* (*get_path)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_KDouble (*get_size)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_kref_dev_rolandh_krfiles_Sorting (*get_sorting)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              const char* (*get_type)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              const char* (*component1)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_kref_kotlin_collections_List (*component10)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_KInt (*component11)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_KInt (*component12)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_kref_dev_rolandh_krfiles_Sorting (*component13)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_KDouble (*component2)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              const char* (*component3)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              const char* (*component4)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_KDouble (*component5)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_KBoolean (*component6)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_KBoolean (*component7)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              const char* (*component8)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              const char* (*component9)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              libkrfiles_kref_dev_rolandh_krfiles_Resource (*copy)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz, const char* name, libkrfiles_KDouble size, const char* extension, const char* modified, libkrfiles_KDouble mode, libkrfiles_KBoolean isDir, libkrfiles_KBoolean isSymlink, const char* type, const char* path, libkrfiles_kref_kotlin_collections_List items, libkrfiles_KInt numDirs, libkrfiles_KInt numFiles, libkrfiles_kref_dev_rolandh_krfiles_Sorting sorting);
              libkrfiles_KBoolean (*equals)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz, libkrfiles_kref_kotlin_Any other);
              libkrfiles_KInt (*hashCode)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
              const char* (*toString)(libkrfiles_kref_dev_rolandh_krfiles_Resource thiz);
            } Resource;
            struct {
              struct {
                libkrfiles_KType* (*_type)(void);
                libkrfiles_kref_dev_rolandh_krfiles_Sorting_$serializer (*_instance)();
                libkrfiles_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libkrfiles_kref_dev_rolandh_krfiles_Sorting_$serializer thiz);
                libkrfiles_kref_kotlin_Array (*childSerializers)(libkrfiles_kref_dev_rolandh_krfiles_Sorting_$serializer thiz);
                libkrfiles_kref_dev_rolandh_krfiles_Sorting (*deserialize)(libkrfiles_kref_dev_rolandh_krfiles_Sorting_$serializer thiz, libkrfiles_kref_kotlinx_serialization_encoding_Decoder decoder);
                void (*serialize)(libkrfiles_kref_dev_rolandh_krfiles_Sorting_$serializer thiz, libkrfiles_kref_kotlinx_serialization_encoding_Encoder encoder, libkrfiles_kref_dev_rolandh_krfiles_Sorting value);
              } $serializer;
              struct {
                libkrfiles_KType* (*_type)(void);
                libkrfiles_kref_dev_rolandh_krfiles_Sorting_Companion (*_instance)();
                libkrfiles_kref_kotlinx_serialization_KSerializer (*serializer)(libkrfiles_kref_dev_rolandh_krfiles_Sorting_Companion thiz);
              } Companion;
              libkrfiles_KType* (*_type)(void);
              libkrfiles_kref_dev_rolandh_krfiles_Sorting (*Sorting)(const char* by, libkrfiles_KBoolean asc);
              libkrfiles_KBoolean (*get_asc)(libkrfiles_kref_dev_rolandh_krfiles_Sorting thiz);
              const char* (*get_by)(libkrfiles_kref_dev_rolandh_krfiles_Sorting thiz);
              const char* (*component1)(libkrfiles_kref_dev_rolandh_krfiles_Sorting thiz);
              libkrfiles_KBoolean (*component2)(libkrfiles_kref_dev_rolandh_krfiles_Sorting thiz);
              libkrfiles_kref_dev_rolandh_krfiles_Sorting (*copy)(libkrfiles_kref_dev_rolandh_krfiles_Sorting thiz, const char* by, libkrfiles_KBoolean asc);
              libkrfiles_KBoolean (*equals)(libkrfiles_kref_dev_rolandh_krfiles_Sorting thiz, libkrfiles_kref_kotlin_Any other);
              libkrfiles_KInt (*hashCode)(libkrfiles_kref_dev_rolandh_krfiles_Sorting thiz);
              const char* (*toString)(libkrfiles_kref_dev_rolandh_krfiles_Sorting thiz);
            } Sorting;
            struct {
              struct {
                libkrfiles_KType* (*_type)(void);
                libkrfiles_kref_dev_rolandh_krfiles_SearchResult_$serializer (*_instance)();
                libkrfiles_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libkrfiles_kref_dev_rolandh_krfiles_SearchResult_$serializer thiz);
                libkrfiles_kref_kotlin_Array (*childSerializers)(libkrfiles_kref_dev_rolandh_krfiles_SearchResult_$serializer thiz);
                libkrfiles_kref_dev_rolandh_krfiles_SearchResult (*deserialize)(libkrfiles_kref_dev_rolandh_krfiles_SearchResult_$serializer thiz, libkrfiles_kref_kotlinx_serialization_encoding_Decoder decoder);
                void (*serialize)(libkrfiles_kref_dev_rolandh_krfiles_SearchResult_$serializer thiz, libkrfiles_kref_kotlinx_serialization_encoding_Encoder encoder, libkrfiles_kref_dev_rolandh_krfiles_SearchResult value);
              } $serializer;
              struct {
                libkrfiles_KType* (*_type)(void);
                libkrfiles_kref_dev_rolandh_krfiles_SearchResult_Companion (*_instance)();
                libkrfiles_kref_kotlinx_serialization_KSerializer (*serializer)(libkrfiles_kref_dev_rolandh_krfiles_SearchResult_Companion thiz);
              } Companion;
              libkrfiles_KType* (*_type)(void);
              libkrfiles_kref_dev_rolandh_krfiles_SearchResult (*SearchResult)(const char* path, libkrfiles_KBoolean dir);
              libkrfiles_KBoolean (*get_dir)(libkrfiles_kref_dev_rolandh_krfiles_SearchResult thiz);
              const char* (*get_path)(libkrfiles_kref_dev_rolandh_krfiles_SearchResult thiz);
              const char* (*component1)(libkrfiles_kref_dev_rolandh_krfiles_SearchResult thiz);
              libkrfiles_KBoolean (*component2)(libkrfiles_kref_dev_rolandh_krfiles_SearchResult thiz);
              libkrfiles_kref_dev_rolandh_krfiles_SearchResult (*copy)(libkrfiles_kref_dev_rolandh_krfiles_SearchResult thiz, const char* path, libkrfiles_KBoolean dir);
              libkrfiles_KBoolean (*equals)(libkrfiles_kref_dev_rolandh_krfiles_SearchResult thiz, libkrfiles_kref_kotlin_Any other);
              libkrfiles_KInt (*hashCode)(libkrfiles_kref_dev_rolandh_krfiles_SearchResult thiz);
              const char* (*toString)(libkrfiles_kref_dev_rolandh_krfiles_SearchResult thiz);
            } SearchResult;
            struct {
              struct {
                libkrfiles_KType* (*_type)(void);
                libkrfiles_kref_dev_rolandh_krfiles_User_$serializer (*_instance)();
                libkrfiles_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libkrfiles_kref_dev_rolandh_krfiles_User_$serializer thiz);
                libkrfiles_kref_kotlin_Array (*childSerializers)(libkrfiles_kref_dev_rolandh_krfiles_User_$serializer thiz);
                libkrfiles_kref_dev_rolandh_krfiles_User (*deserialize)(libkrfiles_kref_dev_rolandh_krfiles_User_$serializer thiz, libkrfiles_kref_kotlinx_serialization_encoding_Decoder decoder);
                void (*serialize)(libkrfiles_kref_dev_rolandh_krfiles_User_$serializer thiz, libkrfiles_kref_kotlinx_serialization_encoding_Encoder encoder, libkrfiles_kref_dev_rolandh_krfiles_User value);
              } $serializer;
              struct {
                libkrfiles_KType* (*_type)(void);
                libkrfiles_kref_dev_rolandh_krfiles_User_Companion (*_instance)();
                libkrfiles_kref_kotlinx_serialization_KSerializer (*serializer)(libkrfiles_kref_dev_rolandh_krfiles_User_Companion thiz);
              } Companion;
              libkrfiles_KType* (*_type)(void);
              libkrfiles_kref_dev_rolandh_krfiles_User (*User)(libkrfiles_KInt id, const char* username, const char* scope, const char* locale, libkrfiles_kref_dev_rolandh_krfiles_Permissions perm, libkrfiles_KBoolean lockPassword, const char* viewMode, libkrfiles_KBoolean singleClick, libkrfiles_KBoolean hideDotfiles, libkrfiles_KBoolean dateFormat);
              libkrfiles_KBoolean (*get_dateFormat)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              libkrfiles_KBoolean (*get_hideDotfiles)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              libkrfiles_KInt (*get_id)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              const char* (*get_locale)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              libkrfiles_KBoolean (*get_lockPassword)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              libkrfiles_kref_dev_rolandh_krfiles_Permissions (*get_perm)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              const char* (*get_scope)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              libkrfiles_KBoolean (*get_singleClick)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              const char* (*get_username)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              const char* (*get_viewMode)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              libkrfiles_KInt (*component1)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              libkrfiles_KBoolean (*component10)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              const char* (*component2)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              const char* (*component3)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              const char* (*component4)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              libkrfiles_kref_dev_rolandh_krfiles_Permissions (*component5)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              libkrfiles_KBoolean (*component6)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              const char* (*component7)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              libkrfiles_KBoolean (*component8)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              libkrfiles_KBoolean (*component9)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              libkrfiles_kref_dev_rolandh_krfiles_User (*copy)(libkrfiles_kref_dev_rolandh_krfiles_User thiz, libkrfiles_KInt id, const char* username, const char* scope, const char* locale, libkrfiles_kref_dev_rolandh_krfiles_Permissions perm, libkrfiles_KBoolean lockPassword, const char* viewMode, libkrfiles_KBoolean singleClick, libkrfiles_KBoolean hideDotfiles, libkrfiles_KBoolean dateFormat);
              libkrfiles_KBoolean (*equals)(libkrfiles_kref_dev_rolandh_krfiles_User thiz, libkrfiles_kref_kotlin_Any other);
              libkrfiles_KInt (*hashCode)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
              const char* (*toString)(libkrfiles_kref_dev_rolandh_krfiles_User thiz);
            } User;
            struct {
              struct {
                libkrfiles_KType* (*_type)(void);
                libkrfiles_kref_dev_rolandh_krfiles_Permissions_$serializer (*_instance)();
                libkrfiles_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libkrfiles_kref_dev_rolandh_krfiles_Permissions_$serializer thiz);
                libkrfiles_kref_kotlin_Array (*childSerializers)(libkrfiles_kref_dev_rolandh_krfiles_Permissions_$serializer thiz);
                libkrfiles_kref_dev_rolandh_krfiles_Permissions (*deserialize)(libkrfiles_kref_dev_rolandh_krfiles_Permissions_$serializer thiz, libkrfiles_kref_kotlinx_serialization_encoding_Decoder decoder);
                void (*serialize)(libkrfiles_kref_dev_rolandh_krfiles_Permissions_$serializer thiz, libkrfiles_kref_kotlinx_serialization_encoding_Encoder encoder, libkrfiles_kref_dev_rolandh_krfiles_Permissions value);
              } $serializer;
              struct {
                libkrfiles_KType* (*_type)(void);
                libkrfiles_kref_dev_rolandh_krfiles_Permissions_Companion (*_instance)();
                libkrfiles_kref_kotlinx_serialization_KSerializer (*serializer)(libkrfiles_kref_dev_rolandh_krfiles_Permissions_Companion thiz);
              } Companion;
              libkrfiles_KType* (*_type)(void);
              libkrfiles_kref_dev_rolandh_krfiles_Permissions (*Permissions)(libkrfiles_KBoolean admin, libkrfiles_KBoolean execute, libkrfiles_KBoolean create, libkrfiles_KBoolean rename, libkrfiles_KBoolean modify, libkrfiles_KBoolean delete_, libkrfiles_KBoolean share, libkrfiles_KBoolean download);
              libkrfiles_KBoolean (*get_admin)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              libkrfiles_KBoolean (*get_create)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              libkrfiles_KBoolean (*get_delete)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              libkrfiles_KBoolean (*get_download)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              libkrfiles_KBoolean (*get_execute)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              libkrfiles_KBoolean (*get_modify)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              libkrfiles_KBoolean (*get_rename)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              libkrfiles_KBoolean (*get_share)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              libkrfiles_KBoolean (*component1)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              libkrfiles_KBoolean (*component2)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              libkrfiles_KBoolean (*component3)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              libkrfiles_KBoolean (*component4)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              libkrfiles_KBoolean (*component5)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              libkrfiles_KBoolean (*component6)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              libkrfiles_KBoolean (*component7)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              libkrfiles_KBoolean (*component8)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              libkrfiles_kref_dev_rolandh_krfiles_Permissions (*copy)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz, libkrfiles_KBoolean admin, libkrfiles_KBoolean execute, libkrfiles_KBoolean create, libkrfiles_KBoolean rename, libkrfiles_KBoolean modify, libkrfiles_KBoolean delete_, libkrfiles_KBoolean share, libkrfiles_KBoolean download);
              libkrfiles_KBoolean (*equals)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz, libkrfiles_kref_kotlin_Any other);
              libkrfiles_KInt (*hashCode)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
              const char* (*toString)(libkrfiles_kref_dev_rolandh_krfiles_Permissions thiz);
            } Permissions;
            struct {
              struct {
                libkrfiles_KType* (*_type)(void);
                libkrfiles_kref_dev_rolandh_krfiles_UserData_$serializer (*_instance)();
                libkrfiles_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libkrfiles_kref_dev_rolandh_krfiles_UserData_$serializer thiz);
                libkrfiles_kref_kotlin_Array (*childSerializers)(libkrfiles_kref_dev_rolandh_krfiles_UserData_$serializer thiz);
                libkrfiles_kref_dev_rolandh_krfiles_UserData (*deserialize)(libkrfiles_kref_dev_rolandh_krfiles_UserData_$serializer thiz, libkrfiles_kref_kotlinx_serialization_encoding_Decoder decoder);
                void (*serialize)(libkrfiles_kref_dev_rolandh_krfiles_UserData_$serializer thiz, libkrfiles_kref_kotlinx_serialization_encoding_Encoder encoder, libkrfiles_kref_dev_rolandh_krfiles_UserData value);
              } $serializer;
              struct {
                libkrfiles_KType* (*_type)(void);
                libkrfiles_kref_dev_rolandh_krfiles_UserData_Companion (*_instance)();
                libkrfiles_kref_kotlinx_serialization_KSerializer (*serializer)(libkrfiles_kref_dev_rolandh_krfiles_UserData_Companion thiz);
              } Companion;
              libkrfiles_KType* (*_type)(void);
              libkrfiles_kref_dev_rolandh_krfiles_UserData (*UserData)(const char* username, const char* password, const char* scope, const char* locale, libkrfiles_kref_dev_rolandh_krfiles_Permissions perm);
              const char* (*get_locale)(libkrfiles_kref_dev_rolandh_krfiles_UserData thiz);
              const char* (*get_password)(libkrfiles_kref_dev_rolandh_krfiles_UserData thiz);
              libkrfiles_kref_dev_rolandh_krfiles_Permissions (*get_perm)(libkrfiles_kref_dev_rolandh_krfiles_UserData thiz);
              const char* (*get_scope)(libkrfiles_kref_dev_rolandh_krfiles_UserData thiz);
              const char* (*get_username)(libkrfiles_kref_dev_rolandh_krfiles_UserData thiz);
              const char* (*component1)(libkrfiles_kref_dev_rolandh_krfiles_UserData thiz);
              const char* (*component2)(libkrfiles_kref_dev_rolandh_krfiles_UserData thiz);
              const char* (*component3)(libkrfiles_kref_dev_rolandh_krfiles_UserData thiz);
              const char* (*component4)(libkrfiles_kref_dev_rolandh_krfiles_UserData thiz);
              libkrfiles_kref_dev_rolandh_krfiles_Permissions (*component5)(libkrfiles_kref_dev_rolandh_krfiles_UserData thiz);
              libkrfiles_kref_dev_rolandh_krfiles_UserData (*copy)(libkrfiles_kref_dev_rolandh_krfiles_UserData thiz, const char* username, const char* password, const char* scope, const char* locale, libkrfiles_kref_dev_rolandh_krfiles_Permissions perm);
              libkrfiles_KBoolean (*equals)(libkrfiles_kref_dev_rolandh_krfiles_UserData thiz, libkrfiles_kref_kotlin_Any other);
              libkrfiles_KInt (*hashCode)(libkrfiles_kref_dev_rolandh_krfiles_UserData thiz);
              const char* (*toString)(libkrfiles_kref_dev_rolandh_krfiles_UserData thiz);
            } UserData;
            struct {
              struct {
                libkrfiles_KType* (*_type)(void);
                libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError_$serializer (*_instance)();
                libkrfiles_kref_kotlinx_serialization_descriptors_SerialDescriptor (*get_descriptor)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError_$serializer thiz);
                libkrfiles_kref_kotlin_Array (*childSerializers)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError_$serializer thiz);
                libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError (*deserialize)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError_$serializer thiz, libkrfiles_kref_kotlinx_serialization_encoding_Decoder decoder);
                void (*serialize)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError_$serializer thiz, libkrfiles_kref_kotlinx_serialization_encoding_Encoder encoder, libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError value);
              } $serializer;
              struct {
                libkrfiles_KType* (*_type)(void);
                libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError_Companion (*_instance)();
                libkrfiles_kref_kotlinx_serialization_KSerializer (*serializer)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError_Companion thiz);
              } Companion;
              libkrfiles_KType* (*_type)(void);
              libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError (*FilebrowserError)(const char* message, libkrfiles_KInt status);
              const char* (*get_message)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError thiz);
              libkrfiles_KInt (*get_status)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError thiz);
              const char* (*component1)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError thiz);
              libkrfiles_KInt (*component2)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError thiz);
              libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError (*copy)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError thiz, const char* message, libkrfiles_KInt status);
              libkrfiles_KBoolean (*equals)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError thiz, libkrfiles_kref_kotlin_Any other);
              libkrfiles_KInt (*hashCode)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError thiz);
              const char* (*toString)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserError thiz);
            } FilebrowserError;
            struct {
              libkrfiles_KType* (*_type)(void);
              libkrfiles_kref_dev_rolandh_krfiles_FilebrowserException (*FilebrowserException)(libkrfiles_KInt statusCode, const char* errorMessage);
              const char* (*get_errorMessage)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserException thiz);
              libkrfiles_KInt (*get_statusCode)(libkrfiles_kref_dev_rolandh_krfiles_FilebrowserException thiz);
            } FilebrowserException;
            libkrfiles_KBoolean (*nativeCopy)(const char* source, const char* destination, libkrfiles_KBoolean override_);
            void (*nativeCreateClient)(const char* baseUrl);
            libkrfiles_KBoolean (*nativeCreateDirectory)(const char* path);
            libkrfiles_KBoolean (*nativeDelete)(const char* path);
            void (*nativeDestroyClient)();
            libkrfiles_KBoolean (*nativeDownloadToFile)(const char* remotePath, const char* localPath);
            const char* (*nativeGetLastError)();
            const char* (*nativeGetResource)(const char* path);
            libkrfiles_KBoolean (*nativeIsAuthenticated)();
            const char* (*nativeListDirectory)(const char* path);
            const char* (*nativeLogin)(const char* username, const char* password);
            libkrfiles_KBoolean (*nativeLogout)();
            libkrfiles_KBoolean (*nativeRename)(const char* source, const char* destination, libkrfiles_KBoolean override_);
            const char* (*nativeSearch)(const char* query, const char* path);
            libkrfiles_KBoolean (*nativeSetToken)(const char* token);
            libkrfiles_KBoolean (*nativeUploadFromFile)(const char* remotePath, const char* localPath, libkrfiles_KBoolean override_);
            libkrfiles_kref_dev_rolandh_krfiles_AuthStorage (*createPlatformAuthStorage)();
          } krfiles;
        } rolandh;
      } dev;
    } root;
  } kotlin;
} libkrfiles_ExportedSymbols;
extern libkrfiles_ExportedSymbols* libkrfiles_symbols(void);
#ifdef __cplusplus
}  /* extern "C" */
#endif
#endif  /* KONAN_LIBKRFILES_H */
