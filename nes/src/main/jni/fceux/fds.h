extern bool isFDS;
void FDSSoundReset(void);

void FCEU_FDSInsert(void);
//void FCEU_FDSEject(void);
void FCEU_FDSSelect(void);

uint8 NOSTALGIA_GetInDisk();
uint8 NOSTALGIA_GetSelDisk();
bool NOSTALGIA_IsFDSInited();
uint8 NOSTALGIA_GetTotalSides();
